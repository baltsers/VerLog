'''
This module is responsible for matching method (according to their names and line numbers) to hunks.
'''
from abc import ABC, abstractmethod
import logging
import re
from utils import string_util


def hunk_matcher_factory(method_changed_category):
    if method_changed_category.startswith('ADDED'):
        return AddedMethodMatchStrategy()
    elif method_changed_category.startswith('MODIFIED'):
        return ModifiedMethodMatchStrategy()
    elif method_changed_category.startswith('DELETED'):
        return DeletedMethodMatchStrategy()
    else:
        raise ValueError('Invalid method changed category: ' + method_changed_category)

def generate_hunk_header(hunk):
    # @@ -42,9 +42,9 @@
    return f'@@ -{hunk.source_start},{hunk.source_length} +{hunk.target_start},{hunk.target_length} @@ \n'


class HunkMatcher():
    def __init__(self, strategy):
        self.strategy = strategy
    
    def set_match_strategy(self, strategy):
        self.strategy = strategy

    def get_matched_lines(self, method_signature, method_line_nums, hunks):
        return self.strategy.get_matched_lines(method_signature, method_line_nums, hunks)

class MatchStrategy(ABC):
    @abstractmethod
    def get_matched_lines(self, method_signature, method_line_nums, hunks):
        pass

    


class ModifiedMethodMatchStrategy(MatchStrategy):

    def __init__(self):
        pass

    @staticmethod
    def extract_method_lines_from_hunk(hunk, method_signature, method_length):
        method_name = string_util.extract_function_name_from_method_signature(method_signature)
        java_function_pattern = r"(public|protected|private)?\s*(static|final)?\s*[\w<>\[\]]+\s+" + re.escape(method_name) + r"\s*\((?:.|\n)*?\)";
        method_declare_idx = None
        for i, line in enumerate(hunk):
            if re.search(java_function_pattern, line.value):
                method_declare_idx = i
                break
        if method_declare_idx is not None:
            added_or_unchnaged_line_length = method_length
            lines = []
            for line in hunk[method_declare_idx:]:
                if added_or_unchnaged_line_length <= 0:
                    break
                lines.append(line)
                if not line.is_removed:
                    added_or_unchnaged_line_length -= 1
            return lines
        else:
            return []
        
    def get_matched_lines(self, method_signature, method_line_nums, hunks):
        logger = logging.getLogger("verlog")
        # Suppose a modified method is in [StartLine, EndLine]
        # Then it should match all the hunks whose target line numbers are in [StartLine, EndLine]
        method_start_line, method_end_line = method_line_nums
        matched_lines = []
        for hunk in hunks:
            # the method_line_nums is the line numbers of the modified method in the target file
            # CASE: 1 modified method - 1 hunk
            if hunk.target_start >= method_start_line and hunk.target_start + hunk.target_length <= method_end_line:
                #print(method_signature, method_start_line, method_end_line, hunk.target_start, hunk.target_start + hunk.target_length)
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                matched_lines.append(generate_hunk_header(hunk))
                for line in hunk:
                    # Match the entire hunk
                    matched_lines.append(line)
            # CASE; n modified methods - 1 hunk
            # Whole method is in the hunk, but the hunk contains multiple methods
            elif hunk.target_start <= method_start_line and hunk.target_start + hunk.target_length >= method_end_line:
                #print(method_signature, method_start_line, method_end_line, hunk.target_start, hunk.target_start + hunk.target_length)
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                matched_lines.append(generate_hunk_header(hunk))
                for line in ModifiedMethodMatchStrategy.extract_method_lines_from_hunk(hunk, method_signature, method_end_line - method_start_line + 1):
                    matched_lines.append(line)
            # CASE: 1 modified method - n hunks ()
            # See /Users/jiawei/Documents/PhD-projects/VerLog/Dataset/test/gitRepo/party-up/ v1.4.0-v1.5.0 handleSendImage() method for an example
            # First Hunk: Method declaration and part of the method body are in the first hunk, and the rest of the method body is in the second hunk
            elif hunk.target_start <= method_start_line and  method_start_line <= hunk.target_start + hunk.target_length <= method_end_line:
                #print(method_signature, method_start_line, method_end_line, hunk.target_start, hunk.target_start + hunk.target_length)
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                matched_lines.append(generate_hunk_header(hunk))
                for line in ModifiedMethodMatchStrategy.extract_method_lines_from_hunk(hunk, method_signature, method_end_line - method_start_line + 1):
                    matched_lines.append(line)

            # Second Hunk: Method declaration and some of the method body are in the first hunk, and the rest of the method body is in the second hunk
            elif method_start_line <= hunk.target_start <= method_end_line and hunk.target_start + hunk.target_length >= method_end_line:
                #print(method_signature, method_start_line, method_end_line, hunk.target_start, hunk.target_start + hunk.target_length)
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                method_tail_length = method_end_line - hunk.target_start + 1
                matched_lines.append(generate_hunk_header(hunk))
                added_or_unchnaged_line_length = method_tail_length
                for line in hunk:
                    if added_or_unchnaged_line_length <= 0:
                        break
                    matched_lines.append(line)
                    if not line.is_removed:
                        added_or_unchnaged_line_length -= 1
            # Since we have already covered the case of 1 modified method - 1 hunk, we don't need to consider this case

        return matched_lines
    
class AddedMethodMatchStrategy(MatchStrategy):
    def __init__(self):
        pass

    def get_matched_lines(self, method_signature, method_line_nums, hunks):
        logger = logging.getLogger("verlog")
        # Suppose an added method is in [StartLine, EndLine]
        method_start_line, method_end_line = method_line_nums
        method_length = method_end_line - method_start_line + 1
        matched_lines = []
        # 1. One hunk may contain multiple added methods
        for hunk in hunks:
            if hunk.target_start <= method_start_line and hunk.target_start + hunk.target_length >= method_end_line:
                # Only return the matched lines
                # Find the method start line in hunk by searching the method name (by regex)
                #print(method_signature, method_start_line, method_end_line, hunk.target_start, hunk.target_start+hunk.target_length)
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                method_name = string_util.extract_function_name_from_method_signature(method_signature)
                java_function_pattern = r"(public|protected|private)?\s*(static|final)?\s*[\w<>\[\]]+\s+" + re.escape(method_name) + r"\s*\((?:.|\n)*?\)"
                method_declare_idx = None
                for i, line in enumerate(hunk):
                    if line.is_added and re.search(java_function_pattern, line.value):
                        method_declare_idx = i
                        break
                if method_declare_idx is not None:
                    matched_lines.append(generate_hunk_header(hunk))
                    added_or_unchnaged_line_length = method_length
                    for line in hunk[method_declare_idx:]:
                        if added_or_unchnaged_line_length <= 0:
                            break
                        matched_lines.append(line)
                        if not line.is_removed:
                            added_or_unchnaged_line_length -= 1

            # This can happen when an added method is actually a refactored method
            elif hunk.target_start <= method_start_line and method_start_line <= hunk.target_start + hunk.target_length <= method_end_line:
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                method_name = string_util.extract_function_name_from_method_signature(method_signature)
                java_function_pattern = r"(public|protected|private)?\s*(static|final)?\s*[\w<>\[\]]+\s+" + re.escape(method_name) + r"\s*\((?:.|\n)*?\)"
                method_declare_idx = None
                for i, line in enumerate(hunk):
                    if line.is_added and re.search(java_function_pattern, line.value):
                        method_declare_idx = i
                        break
                if method_declare_idx is not None:
                    matched_lines.append(generate_hunk_header(hunk))
                    for line in hunk[method_declare_idx:]:
                        matched_lines.append(line)
            
            # Same as modified method, the added method may be split into multiple hunks
            elif method_start_line <= hunk.target_start <= method_end_line and hunk.target_start + hunk.target_length >= method_end_line:
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                method_tail_length = method_end_line - hunk.target_start + 1
                matched_lines.append(generate_hunk_header(hunk))
                added_or_unchnaged_line_length = method_tail_length
                for line in hunk:
                    if added_or_unchnaged_line_length <= 0:
                        break
                    matched_lines.append(line)
                    if not line.is_removed:
                        added_or_unchnaged_line_length -= 1
                        


        # 2. One hunk only contains one added method
        # The approach above already covers this case
        return matched_lines
    


class DeletedMethodMatchStrategy(MatchStrategy):
    def __init__(self):
        pass

    def get_matched_lines(self, method_signature, method_line_nums, hunks):  
        logger = logging.getLogger("verlog")   
        # Similar to added method, but the target line numbers are replaced by source line numbers
        method_start_line, method_end_line = method_line_nums
        method_length = method_end_line - method_start_line + 1
        matched_lines = []
        # 1. One hunk may contain multiple deleted methods
        for hunk in hunks:
            if hunk.source_start <= method_start_line and hunk.source_start + hunk.source_length >= method_end_line:
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                # Only return the matched lines
                # Find the method start line in hunk by searching the method name (by regex)
                method_name = string_util.extract_function_name_from_method_signature(method_signature)
                java_function_pattern = r"(public|protected|private)?\s*(static|final)?\s*[\w<>\[\]]+\s+" + re.escape(method_name) + r"\s*\((?:.|\n)*?\)";
                method_declare_idx = None
                for i, line in enumerate(hunk):
                    if line.line_type == "-" and re.search(java_function_pattern, line.value):
                        method_declare_idx = i
                        break
                if method_declare_idx is not None:
                    matched_lines.append(generate_hunk_header(hunk))
                    for line in hunk[method_declare_idx: method_declare_idx + method_length]:
                        matched_lines.append(line)
        
        # This can happen when a deleted method is actually a refactored method, same as added method
            elif hunk.source_start <= method_start_line and method_start_line <= hunk.source_start + hunk.source_length <= method_end_line:
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                method_name = string_util.extract_function_name_from_method_signature(method_signature)
                java_function_pattern = r"(public|protected|private)?\s*(static|final)?\s*[\w<>\[\]]+\s+" + re.escape(method_name) + r"\s*\((?:.|\n)*?\)"
                method_declare_idx = None
                for i, line in enumerate(hunk):
                    if line.line_type == "-" and re.search(java_function_pattern, line.value):
                        method_declare_idx = i
                        break
                if method_declare_idx is not None:
                    matched_lines.append(generate_hunk_header(hunk))
                    for line in hunk[method_declare_idx:]:
                        matched_lines.append(line)

            # Same as modified method, the deleted method may be split into multiple hunks
            elif method_start_line <= hunk.source_start <= method_end_line and hunk.source_start + hunk.source_length >= method_end_line:
                logger.debug(f"{method_signature} StartLine: {method_start_line}, EndLine: {method_end_line}, HunkStartLine: {hunk.target_start}, HunkEndLine: {hunk.target_start + hunk.target_length}")
                method_tail_length = method_end_line - hunk.target_start + 1
                matched_lines.append(generate_hunk_header(hunk))
                added_or_unchnaged_line_length = method_tail_length
                for line in hunk:
                    if added_or_unchnaged_line_length <= 0:
                        break
                    matched_lines.append(line)
                    if not line.is_removed:
                        added_or_unchnaged_line_length -= 1

        # 2. One hunk only contains one deleted method
        # The approach above already covers this case
        return matched_lines