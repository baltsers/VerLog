def find_entry_methods(augmented_methods):
    # Step 1: Collect all reachable methods from all augmented methods
    all_reachable_methods = set()
    for augmented_method in augmented_methods:
        all_reachable_methods.update(augmented_method.reachable_methods)

    # Step 2: Identify entry methods
    entry_methods = []
    for augmented_method in augmented_methods:
        if augmented_method.method_name not in all_reachable_methods:
                entry_methods.append(augmented_method)
    
        
    return entry_methods

class AugmentedMethodNode:
    def __init__(self, method, is_augmented=True):
        self.method = method  # Can be an augmented method or a non-augmented method
        self.is_augmented = is_augmented  # Indicates whether the method is augmented
        self.children = []

    def add_child(self, child_node):
        self.children.append(child_node)

class SliceTree:
    def __init__(self, root):
        self.root = root

    def add_child(self, child_node):
        self.root.children.append(child_node)

    def pretty_print(self):
        self.pretty_print_tree(self.root)

    def pretty_print_tree(self, node, indent=0):
        # if node.is_augmented:
        #     print('  ' * indent + node)
        # else:
        #     print('  ' * indent + node)
        print(node)
        for child in node.children:
            self.pretty_print_tree(child, indent + 1)

def is_valid_reachable_method(method_name):
    return all(not method_name.startswith(prefix) for prefix in ["<java.", "<javax.", "<sun.", "<com.sun."]) and method_name.find("init>") == -1

def explore_reachable_methods_as_tree(method, augmented_methods, visited, is_augmented=True):
    """
    Recursively explore reachable methods (both augmented and non-augmented) from a given method and build a tree.
    """
    if is_augmented and method.method_name in visited:
        return None
    if is_augmented:
        visited.add(method.method_name)

    node = AugmentedMethodNode(method, is_augmented)
    if is_augmented:
        for reachable_method_name in method.reachable_methods:
            child_node = None
            # Check if the reachable method is augmented
            for am in augmented_methods:
                if am.method_name == reachable_method_name:
                    child_node = explore_reachable_methods_as_tree(am, augmented_methods, visited, True)
                    break
            # If the method is not augmented, create a leaf node
            if child_node is None and is_valid_reachable_method(reachable_method_name):
                child_node = AugmentedMethodNode(reachable_method_name, is_augmented=False)
            if child_node:
                node.add_child(child_node)
    return node

def build_entry_methods_tree(entry_methods, augmented_methods):
    """
    For each entry method, build a tree of all methods (augmented and non-augmented) reachable from it.
    """
    trees = []
    for entry_method in entry_methods:
        visited = set()
        tree_root = explore_reachable_methods_as_tree(entry_method, augmented_methods, visited, True)
        if tree_root:
            trees.append(SliceTree(tree_root))

    return trees

def pretty_print_tree(node, depth=0):
    indent = "  " * depth
    method_type = "Contextualized" if node.is_augmented else "Non-contextualized"
    print(f"{indent}- {method_type}: {node.method.method_name if node.is_augmented else node.method}")
    for child in node.children:
        pretty_print_tree(child, depth + 1)
