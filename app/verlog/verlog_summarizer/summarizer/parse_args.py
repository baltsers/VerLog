import argparse

def parse_summarizor_args(parser: argparse.ArgumentParser):
    parser.add_argument('--input-prompt-file', type=str, required=True)
    parser.add_argument('--model', type=str, required=False, choices=['ChatGPT', 'CodeLlama', 'DeepSeek'])
    parser.add_argument('--exact-model-name', type=str, default="gpt-4o-mini")
    parser.add_argument('--output-dir', type=str, required=True)
    parser.add_argument('--system-prompt-file', type=str, required=True)
