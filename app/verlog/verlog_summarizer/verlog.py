import argparse
from prompt_generator.parse_args import parse_prompt_generator_args
from prompt_generator.run import run as run_prompt_generator
from summarizer.summarize_prompt import run as run_summarizer
from summarizer.parse_args import parse_summarizor_args 

def create_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser()
    parser.add_argument("--log-path", type=str, default=None, help="Path to the log file")
    subparsers = parser.add_subparsers(dest="command")
    
    prompt_generator_parser = subparsers.add_parser("parse")
    parse_prompt_generator_args(prompt_generator_parser)

    summarizer_parser = subparsers.add_parser("summarize")
    parse_summarizor_args(summarizer_parser)

    return parser

def main():
    parser = create_parser()
    args = parser.parse_args()
    if args.command == "parse":
        run_prompt_generator(args)
    elif args.command == "summarize":
        run_summarizer(args)
    else:
        parser.print_help()

if __name__ == "__main__":

    main()


