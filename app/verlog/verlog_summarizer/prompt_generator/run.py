import sys
from prompt_generator import soot_diff_parser
from utils import string_util
from prompt_generator import prompt_generator
import logging
from collections import defaultdict, deque
from .prompt_slicer import slice_prompt, is_long_prompt
from .prompt_cluster import prune_hierarchy_tree


class TreeProcessor:
    @staticmethod
    def generate_trees(augmented_methods):
        # Separate methods into two groups
        ref_methods = [m for m in augmented_methods if
                       m.method_changed_category.endswith("_REF_CLASS") or m.method_changed_category.startswith(
                           "DELETED_")]
        tgt_methods = [m for m in augmented_methods if
                       m.method_changed_category.endswith("_TGT_CLASS") or m.method_changed_category.startswith(
                           "ADDED_")]

        # Find entry methods for each group
        ref_entry_methods = prompt_generator.find_entry_methods(ref_methods)
        tgt_entry_methods = prompt_generator.find_entry_methods(tgt_methods)

        # Build trees for each group
        ref_trees = prompt_generator.build_entry_methods_tree(ref_entry_methods, ref_methods)
        tgt_trees = prompt_generator.build_entry_methods_tree(tgt_entry_methods, tgt_methods)

        return ref_trees, tgt_trees

    @staticmethod
    def merge_trees(tree1, tree2, refactor_merge=False):
        def merge_nodes(node1, node2):
            if refactor_merge:
                merged_method = node2.method
            else:
                merged_method = node1.method

            merged_node = prompt_generator.AugmentedMethodNode(merged_method, node1.is_augmented)

            merged_node.children = []

            children1 = {child.method.method_name if child.is_augmented else child.method: child for child in
                         node1.children}
            children2 = {child.method.method_name if child.is_augmented else child.method: child for child in
                         node2.children}

            all_children = set(children1.keys()) | set(children2.keys())

            for child_method in all_children:
                if child_method in children1 and child_method in children2:
                    merged_node.children.append(merge_nodes(children1[child_method], children2[child_method]))
                elif child_method in children1:
                    child_node = children1[child_method]
                    if child_node.is_augmented:
                        child_node.method.method_name = f"- {child_node.method.method_name}"
                    else:
                        child_node.method = f"- {child_node.method}"
                    merged_node.children.append(child_node)
                else:
                    child_node = children2[child_method]
                    if child_node.is_augmented:
                        child_node.method.method_name = f"+ {child_node.method.method_name}"
                    else:
                        child_node.method = f"+ {child_node.method}"
                    merged_node.children.append(child_node)

            return merged_node

        merged_root = merge_nodes(tree1.root, tree2.root)
        if refactor_merge:
            merged_root.method.method_name = tree1.root.method.method_name + " -> " + tree2.root.method.method_name

        return prompt_generator.SliceTree(merged_root)

    @staticmethod
    def process_trees(ref_trees, tgt_trees):
        merged_and_unique_trees = []

        # Create dictionaries for quick lookup
        ref_dict = {tree.root.method.method_name: tree for tree in ref_trees}
        tgt_dict = {tree.root.method.method_name: tree for tree in tgt_trees}

        relaxed_ref_dict = {string_util.extract_non_parameterized_method_signature(tree.root.method.method_name): tree
                            for tree in ref_trees}
        relaxed_tgt_dict = {string_util.extract_non_parameterized_method_signature(tree.root.method.method_name): tree
                            for tree in tgt_trees}

        # Process all unique root methods
        all_root_methods = set(ref_dict.keys()) | set(tgt_dict.keys())

        refactored_methods = set()
        for root_method in all_root_methods:
            if string_util.extract_non_parameterized_method_signature(root_method) in refactored_methods:
                logger = logging.getLogger("verlog")
                logger.debug(f"Method {root_method} is refactored, skipping.")
                continue

            ref_tree = ref_dict.get(root_method)
            tgt_tree = tgt_dict.get(root_method)

            relaxed_ref_tree = relaxed_ref_dict.get(string_util.extract_non_parameterized_method_signature(root_method))
            relaxed_tgt_tree = relaxed_tgt_dict.get(string_util.extract_non_parameterized_method_signature(root_method))

            if ref_tree and tgt_tree:
                # Method exists in both versions, merge the trees
                merged_tree = TreeProcessor.merge_trees(ref_tree, tgt_tree)
                merged_and_unique_trees.append(('merged', merged_tree))
            elif relaxed_ref_tree and relaxed_tgt_tree:
                # Method exists in both versions, but with different method signatures
                merged_tree = TreeProcessor.merge_trees(relaxed_ref_tree, relaxed_tgt_tree, refactor_merge=True)
                merged_and_unique_trees.append(('rafactor_merged', merged_tree))
                refactored_methods.add(string_util.extract_non_parameterized_method_signature(root_method))
            elif ref_tree:
                # Method only in ref version (deleted)
                ref_tree.root.method.method_name = f"- {ref_tree.root.method.method_name}"
                merged_and_unique_trees.append(('deleted', ref_tree))
            elif tgt_tree:
                # Method only in tgt version (added)
                tgt_tree.root.method.method_name = f"+ {tgt_tree.root.method.method_name}"
                merged_and_unique_trees.append(('added', tgt_tree))

        cleaned_trees = TreeProcessor.remove_descendant_trees(merged_and_unique_trees)

        return cleaned_trees

    @staticmethod
    def remove_descendant_trees(merged_trees):
        def get_non_leafy_nodes(tree):
            non_leafy_nodes = set()

            def traverse(node):
                for child in node.children:
                    if child.children:  # if this child has children, it's a non-leafy node
                        method_name = child.method.method_name if child.is_augmented else child.method
                        # Remove '+' or '-' prefix if present
                        if method_name.startswith('+ ') or method_name.startswith('- '):
                            method_name = method_name[2:]
                        non_leafy_nodes.add(method_name)
                    traverse(child)

            traverse(tree.root)
            return non_leafy_nodes

        # Create a dict {root: [non-leafy-nodes]} for each merged tree
        tree_dict = {tree.root.method.method_name: get_non_leafy_nodes(tree) for _, tree in merged_trees}

        cleaned_trees = []
        for tree_type, tree in merged_trees:
            root_name = tree.root.method.method_name
            is_descendant = any(root_name in non_leafy_nodes
                                for other_root, non_leafy_nodes in tree_dict.items()
                                if other_root != root_name)
            if not is_descendant:
                cleaned_trees.append((tree_type, tree))
            else:
                print(f"Tree {root_name} is detected a descendant of another tree, please verify the results.")

        return cleaned_trees


    @staticmethod
    def get_CMG(tree):
        def bfs_label_and_edges(root):
            queue = deque([(root, 0)])
            visit_order = []
            edges = []
            label = 0

            while queue:
                node, current_label = queue.popleft()
                node.label = current_label

                if node.is_augmented:
                    method_name = node.method.method_name if hasattr(node.method, 'method_name') else str(node.method)
                else:
                    method_name = str(node.method)

                prefix = ""
                if method_name.startswith("- ") or method_name.startswith("+ "):
                    prefix = method_name[:2]
                    method_name = method_name[2:]
                visit_order.append((current_label, prefix, method_name, node))

                for child in node.children:
                    label += 1
                    queue.append((child, label))

                    if child.is_augmented:
                        child_method = child.method.method_name if hasattr(child.method, 'method_name') else str(
                            child.method)
                    else:
                        child_method = str(child.method)

                    edge_prefix = ""
                    if child_method.startswith("- ") or child_method.startswith("+ "):
                        edge_prefix = child_method[:2]
                    edges.append((edge_prefix, current_label, label))

            return visit_order, edges

        visit_order, edges = bfs_label_and_edges(tree.root)

        return visit_order, edges

def get_prompt_fixed_part(tree, app_description, commit_messages):
    prompt_fixed_part = ''
    prompt_fixed_part += f"**App Description**: {app_description}\n"
    prompt_fixed_part += f"**Entry Method Signature**: {tree.root.method.method_name if tree.root.is_augmented else tree.root.method}\n"
    prompt_fixed_part += f"**Method Line Number(start,end)**: {tree.root.method.method_line_nums if tree.root.is_augmented else 'N/A'}\n"
    prompt_fixed_part += "**Commit Messages between two versions**:\n"
    for i, commit_message in enumerate(commit_messages, start=1):
        prompt_fixed_part += f"\t{i}. {commit_message}\n"
    prompt_fixed_part += "**Partial call graph**:\n"
    prompt_fixed_part += "\t- **Nodes(Reachable methods(and their changes) from entry method)**:\n"
    return prompt_fixed_part

def get_prompt_varid_part_clustered(app_description, commit_messages, package_or_class):
    prompt = ""
    prompt += f"**App Description**: {app_description}\n"
    prompt += f"**Package/Class**: {package_or_class}\n"
    prompt += "**Commit Messages between two versions**:\n"
    for i, commit_message in enumerate(commit_messages, start=1):
        prompt += f"\t{i}. {commit_message}\n"
    prompt += "**Changed Methods**:\n"
    return prompt

def get_prompt_varid_part(visit_order, edges):
    prompt = ""
    for label, prefix, method_name, node in visit_order:
        prompt += f"\t\t{label}. {prefix}{method_name}\n"
        if node.is_augmented and hasattr(node.method, 'diff_hunk_lines') and node.method.diff_hunk_lines:
            prompt += "\t\t\tChanged code in this method:\n"
            for line in node.method.diff_hunk_lines:
                prompt += "\t\t\t\t" + str(line)  # + "\n"  # This will use the Line.__str__ method
    if len(edges) > 0:
        prompt += "\t- **Edges(Calling relationship between reachable methods)**:\n"
        for prefix, start, end in edges:
            prompt += f"\t\t{prefix}<{start}, {end}>\n"
    return prompt



def run(args):
    soot_diff_file = args.input
    app_release_tag_1 = args.ref_release_tag
    app_release_tag_2 = args.tgt_release_tag
    repo_path = args.repo_path
    app_description = args.app_description
    token_limit = args.token_limit
    output_dir = args.output_dir
    commit_messages_file = args.commit_messages_file
    reduce_prompts = args.reduce_prompts
    # log_path = args.log_path

    # print("Load tokenizer.....")
    # tokenizer = tiktoken.get_encoding("o200k_base")
    # print("Tokenizer Loaded")


    with open(commit_messages_file, 'r') as f:
        commit_messages_list = f.readlines()

    commit_messages = []
    commit_messages = set(line.strip() for line in commit_messages_list)

    augmented_methods = soot_diff_parser.get_parsed_diff_result(soot_diff_file, app_release_tag_1, app_release_tag_2,
                                                                repo_path)
    trees = TreeProcessor.generate_trees(augmented_methods)
    merged_and_unique_trees = TreeProcessor.process_trees(trees[0], trees[1])

    prompt_size = len(merged_and_unique_trees)
    if not prompt_size:
        sys.exit("No prompt generated")


    if not reduce_prompts:
        # Genearte prompts
        for i, (_, tree) in enumerate(merged_and_unique_trees, start=1):
            visit_order, edges = TreeProcessor.get_CMG(tree)
            class_method_signature = string_util.extract_class_method_name_from_method_signature(tree.root.method.method_name)
            prompt_fixed_part = get_prompt_fixed_part(tree, app_description, commit_messages)
            prompt_varid_part = get_prompt_varid_part(visit_order, edges)
            complete_prompt = prompt_fixed_part + '\n' + prompt_varid_part
            # if is_long_prompt(complete_prompt, token_limit, tokenizer):
            #
            #     sliced_prompts = slice_prompt(prompt_fixed_part, visit_order, edges, tokenizer, token_limit)
            #     for j, prompt in enumerate(sliced_prompts, start=1):
            #         with open(f"{output_dir}/sliced-prompt-{i}-{j}", 'w') as f:
            #             f.write(prompt)
            # else:
            with open(f"{output_dir}/{class_method_signature}.prompt", 'w') as f:
                f.write(complete_prompt)
    else:
        num_nodes_in_cmg: list[tuple[str, int]] = []
        # Generate reduced number of prompts by clustering prompts with only one node
        clustered_prompts: dict[tuple[str, str], list[str]] = defaultdict(list)
        for i, (_, tree) in enumerate(merged_and_unique_trees, start=1):
            visit_order, edges = TreeProcessor.get_CMG(tree)
            class_method_signature = string_util.extract_class_method_name_from_method_signature(tree.root.method.method_name)
            num_nodes_in_cmg.append((class_method_signature, len(visit_order)))
        cluster_mappings = prune_hierarchy_tree(num_nodes_in_cmg) # com.termux.shared.view.ViewUtils.dpToPx -> ('com.termux.shared.view.ViewUtils', 4)
        #     # Print the mappings
        # print("\nMethod Mappings:")
        for i, (_, tree) in enumerate(merged_and_unique_trees, start=1):
            visit_order, edges = TreeProcessor.get_CMG(tree)
            class_method_signature = string_util.extract_class_method_name_from_method_signature(tree.root.method.method_name)
            assert class_method_signature in cluster_mappings
            # One CMG has >1 nodes
            cluster_name = cluster_mappings[class_method_signature][0]
            if class_method_signature == cluster_name:
                prompt_fixed_part = get_prompt_fixed_part(tree, app_description, commit_messages)
                prompt_varid_part = get_prompt_varid_part(visit_order, edges)
                complete_prompt = prompt_fixed_part + '\n' + prompt_varid_part
                with open(f"{output_dir}/{class_method_signature}.prompt", 'w') as f:
                    f.write(complete_prompt)
            else:
                prompt_fixed_part_clustered = get_prompt_varid_part_clustered(app_description, commit_messages, cluster_name)
                prompt_varid_part = get_prompt_varid_part(visit_order, edges)
                clustered_prompts[(cluster_name, prompt_fixed_part_clustered)].append(prompt_varid_part)
        # Write the clustered prompts to files
        for (cluster_name, prompt_fixed_part_clustered), prompt_varid_parts in clustered_prompts.items():
            complete_prompt = prompt_fixed_part_clustered + '\n'
            for prompt_varid_part in prompt_varid_parts:
                complete_prompt += prompt_varid_part + '\n'
            with open(f"{output_dir}/{cluster_name}.prompt", 'w') as f:
                f.write(complete_prompt)
            

               

