import argparse

def parse_prompt_generator_args(parser: argparse.ArgumentParser):
    parser.add_argument('-i', "--input", type=str, required=True, help="Json file of the diff from Phase 1")
    parser.add_argument('-tag1', "--ref-release-tag", type=str, required=True, help="First app release tag")
    parser.add_argument('-tag2', "--tgt-release-tag", type=str, required=True, help="Second app release tag")
    parser.add_argument("--repo-path", type=str, required=True, help="Repository path")
    parser.add_argument('--app-description', type=str, help='Application description')
    parser.add_argument("--token-limit", type=int, default=120_000, help="Number of maximum tokens in a prompt and "
                                                                      "response")
    parser.add_argument('--commit-messages-file', type=str, required=True)
    parser.add_argument('--reduce_prompts', action='store_true', default=False, help='Reduce number of prompts')
    parser.add_argument('-o',"--output-dir", type=str, required=True, help="Output directory")