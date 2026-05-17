'''Deal with potential long prompts that exceed the token limit of the model'''


def get_token_count(prompt: str, tokenizer) -> int:
    '''Count the number of tokens in the prompt'''
    return len(tokenizer.encode(prompt))


def is_long_prompt(prompt: str, max_token: int, tokenizer) -> bool:
    '''Check if the prompt is longer than the token limit of the model'''
    # get the number of tokens in the prompt
    return get_token_count(prompt, tokenizer) > max_token



def slice_prompt(prompt_fixed_header, visit_order, edges, tokenizer, max_token) -> list[str]:
    '''Slice the prompt into smaller parts that fit the token limit of the model using bin packing
    '''
    sliced_prompts = []
    method_statements_dict = {}

    # Prepare fixed tail content
    prompt_fixed_tail = "\t- **Edges(Calling relationship between reachable methods)**:\n"
    for prefix, start, end in edges:
        prompt_fixed_tail += f"\t\t{prefix}<{start}, {end}>\n"

    # Collect method statements and their changed code
    for label, prefix, method_name, node in visit_order:
        changed_statements_optional = ""
        if node.is_augmented and hasattr(node.method, 'diff_hunk_lines') and node.method.diff_hunk_lines:
            changed_statements_optional = "\t\t\tChanged code in this method:\n"
            for line in node.method.diff_hunk_lines:
                changed_statements_optional += "\t\t\t\t" + str(line)

        method_statements_dict[f'\t\t{label}. {prefix}{method_name}\n'] = changed_statements_optional

    # Calculate base content length
    fixed_content = prompt_fixed_header + prompt_fixed_tail + "".join(method_statements_dict.keys())
    base_length = get_token_count(fixed_content, tokenizer)

    # Prepare bins (each prompt slice is a bin)
    bins = []  # List of dictionaries, each dict represents methods in a bin
    bin_sizes = []  # Current token count for each bin

    # Sort methods by their changed content size in descending order
    method_items = [(method, changed, get_token_count(changed, tokenizer))
                    for method, changed in method_statements_dict.items()
                    if changed.strip()]
    method_items.sort(key=lambda x: x[2], reverse=True)

    # First-fit decreasing bin packing
    for method, changed_content, token_count in method_items:
        # Try to fit into existing bin
        placed = False
        for bin_idx, current_size in enumerate(bin_sizes):
            if current_size + token_count <= max_token - base_length:
                bins[bin_idx][method] = changed_content
                bin_sizes[bin_idx] += token_count
                placed = True
                break

        # If couldn't fit in any existing bin, create new bin
        if not placed:
            bins.append({method: changed_content})
            bin_sizes.append(token_count)

    # Generate prompts from bins
    for bin_methods in bins:
        slice_content = [prompt_fixed_header]
        for method_name in method_statements_dict.keys():
            if method_name in bin_methods:
                slice_content.append(f"{method_name}{bin_methods[method_name]}")
            else:
                slice_content.append(method_name)
        slice_content.append(prompt_fixed_tail)

        final_prompt = "".join(slice_content)

        sliced_prompts.append(final_prompt)

    return sliced_prompts
