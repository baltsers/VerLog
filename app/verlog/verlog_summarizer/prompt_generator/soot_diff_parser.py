import json
from utils import string_util
from prompt_generator.method_hunk_matcher import (
    HunkMatcher,
    ModifiedMethodMatchStrategy, 
    AddedMethodMatchStrategy, 
    DeletedMethodMatchStrategy,
    hunk_matcher_factory
)

from prompt_generator import git_diff_parser
from prompt_generator.augmented_method import AugmentedMethod


def __parse_one_added_or_deleted_method(method_info, method_changed_category, diff_hunks):
    '''
    Parse one changed method
    '''
    method_name = method_info['method_name']
    unformatted_method_line_nums = method_info['line_number']
    method_line_nums = string_util.format_method_line_nums(unformatted_method_line_nums)
    reachable_methods = method_info['reachable_methods']
    # Skip methods without line numbers (0-0)
    if method_line_nums == (0, 0):
        return None
    method_hunk_matcher = HunkMatcher(hunk_matcher_factory(method_changed_category))
    matched_lines = method_hunk_matcher.get_matched_lines(method_name, method_line_nums, diff_hunks)
    augmented_method = AugmentedMethod(method_name, 
                                       method_changed_category, 
                                       method_line_nums, 
                                       reachable_methods, 
                                       matched_lines)
    return augmented_method

def __parse_two_modified_methods(method_info_ref, method_info_tgt, method_changed_category, diff_hunks):
    '''
    Parse two modified methods
    '''
    #assert method_info_tgt is not None, f"The method in the target class should not be None, ref method: {method_info_ref['method_name']}"
    method_name = method_info_tgt['method_name']
    unformatted_method_line_nums_ref = method_info_ref['line_number']
    unformatted_method_line_nums_tgt = method_info_tgt['line_number']
    method_line_nums_ref = string_util.format_method_line_nums(unformatted_method_line_nums_ref)
    method_line_nums_tgt = string_util.format_method_line_nums(unformatted_method_line_nums_tgt)
    reachable_methods_ref = method_info_ref['reachable_methods']
    reachable_methods_tgt = method_info_tgt['reachable_methods']
    # Skip methods without line numbers (0-0)
    if method_line_nums_tgt == (0, 0):
        return None
    method_hunk_matcher = HunkMatcher(hunk_matcher_factory(method_changed_category))
    matched_lines = method_hunk_matcher.get_matched_lines(method_name, method_line_nums_tgt, diff_hunks)
    augmented_method_ref = AugmentedMethod(method_name, 'MODIFIED_METHODS_IN_REF_CLASS', method_line_nums_ref, reachable_methods_ref, matched_lines)
    augmented_method_tgt = AugmentedMethod(method_name, 'MODIFIED_METHODS_IN_TGT_CLASS', method_line_nums_tgt, reachable_methods_tgt, matched_lines)
    return augmented_method_ref, augmented_method_tgt

def __get_hunks_from_patch_set(patch_set, file_path):
    '''
    Get hunks from patch set, and the file path contains the class name
    '''
    for file in patch_set:
        if file.path == file_path:
            return file
    return None

def get_parsed_diff_result(soot_diff_result_file, tag1, tag2, repo_path):
    '''
    Parse the diff result and return a more handly result
    '''
    with open(soot_diff_result_file, encoding='utf-8') as f:
        diff_result = json.load(f)
    diff_patchset = git_diff_parser.get_patchset_from_subshell(tag1, tag2, repo_path)

    # Extract added, modified, and deleted activities
    added_activities = [act for act in diff_result['added_classes']]
    modified_activities = [act for act in diff_result['modified_classes']]
    deleted_activities = [act for act in diff_result['deleted_classes']]


    augmented_methods = []

    for activity in modified_activities:
        diff_hunks = __get_hunks_from_patch_set(diff_patchset, activity['class_name'])
        for method in activity['ADDED_METHOD_IN_MODIFIED_CLASS']:
            augmented_method = __parse_one_added_or_deleted_method(method, 'ADDED_METHOD_IN_MODIFIED_CLASS', diff_hunks)
            if augmented_method:
                augmented_methods.append(augmented_method)

        
        # Store the index-method_name pairs to find the corresponding refMethod
        # because the order of methods in ref and tgt may be different
        tgt_method_index_method_name_pairs = [(i, method['method_name']) for i, method in enumerate(activity['MODIFIED_METHOD_IN_TGT_CLASS'])]

        for ref_method in activity['MODIFIED_METHOD_IN_REF_CLASS']:
            method_name = ref_method['method_name']
            tgt_method = None

            # Find the corresponding tgtMethod
            for i, tgt_method_name in tgt_method_index_method_name_pairs:
                if method_name == tgt_method_name or string_util.extract_function_name_from_method_signature(method_name) == string_util.extract_function_name_from_method_signature(tgt_method_name):
                    tgt_method = activity['MODIFIED_METHOD_IN_TGT_CLASS'][i]
                    break

            augmented_ref_method, augmented_tgt_method = __parse_two_modified_methods(ref_method,
                                                                                      tgt_method,
                                                                                      'MODIFIED_METHOD_IN_MODIFIED_CLASS',
                                                                                      diff_hunks)
            if augmented_ref_method and augmented_tgt_method:
                augmented_methods.append(augmented_ref_method)
                augmented_methods.append(augmented_tgt_method)
            

        for method in activity['DELETED_METHOD_IN_MODIFIED_CLASS']:
            augmented_method = __parse_one_added_or_deleted_method(method, 'DELETED_METHOD_IN_MODIFIED_CLASS', diff_hunks)
            if augmented_method:
                augmented_methods.append(augmented_method)

    for activity in added_activities:
        diff_hunks = __get_hunks_from_patch_set(diff_patchset, activity['class_name'])
        for method in activity['ADDED_METHOD_IN_ADDED_CLASS']:
            augmented_method = __parse_one_added_or_deleted_method(method, 'ADDED_METHOD_IN_ADDED_CLASS', diff_hunks)
            if augmented_method:
                augmented_methods.append(augmented_method)

    for activity in deleted_activities:
        diff_hunks = __get_hunks_from_patch_set(diff_patchset, activity['class_name'])
        for method in activity['DELETED_METHOD_IN_DELETED_CLASS']:
            augmented_method = __parse_one_added_or_deleted_method(method, 'DELETED_METHOD_IN_DELETED_CLASS', diff_hunks)
            if augmented_method:
                augmented_methods.append(augmented_method)

    #filtered_augmented_methods = __filter_augmented_methods(augmented_methods)
    
    return augmented_methods

