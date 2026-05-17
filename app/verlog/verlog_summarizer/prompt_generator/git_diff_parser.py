import subprocess
from unidiff import PatchSet


def get_patchset_from_diff_file(git_diff_result_file):
    '''
    Extract hunks from git diff result file
    '''
    patch_set = PatchSet.from_filename(git_diff_result_file)
    return patch_set

def get_patchset_from_subshell(tag1, tag2, path):
    '''
    Extract hunks from git diff result
    '''
    cmd = f"cd {path}; git diff  --ignore-blank-lines --ignore-all-space {tag1} {tag2} -- \'*.java\'"
    output = subprocess.check_output(cmd, shell=True).decode()
    return PatchSet(output)

def get_commit_messages_from_subshell(tag1, tag2, path):
    '''
    Extract commit messages from git log
    '''
    cmd = f"cd {path}; git log --pretty=format:\"%s\" {tag1}..{tag2}"
    output = subprocess.check_output(cmd, shell=True).decode()
    return output.split('\n')