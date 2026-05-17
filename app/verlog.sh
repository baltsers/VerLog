#!/bin/bash

set -euo pipefail

usage () {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --android-sdk-path <path>       Path to the Android SDK"
    echo "  --git-repo <path>                Path to the git repository"
    echo "  --ref-apk <path>                 Path to the reference APK file"
    echo "  --ref-version <version>          Reference version tag"
    echo "  --ref-repo-dir <path>            Path to the reference repository directory"
    echo "  --tgt-apk <path>                 Path to the target APK file"
    echo "  --tgt-version <version>          Target version tag"
    echo "  --tgt-repo-dir <path>            Path to the target repository directory"
    echo "  --app-description <description>  Description of the app"
    echo "  --model <model>                  LLM Model for summarization"
    echo "  --exact-model-name <name>        Exact model name for summarization"
    echo "  --system-prompt-file <file>      System prompt file for summarization"
    echo "  --output-dir <dir>               Output directory for results"
}

if [[ $# -eq 0 ]]; then
    usage
    exit 1
fi


# Parse named arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --android-sdk-path)
            android_sdk_path="$2"
            shift 2
            ;;
        --git-repo)
            git_repo="$2"
            shift 2
            ;;
        --ref-apk)
            ref_apk="$2"
            shift 2
            ;;
        --ref-version)
            ref_version="$2"
            shift 2
            ;;
        --ref-repo-dir)
            ref_repo_dir="$2"
            shift 2
            ;;
        --tgt-apk)
            tgt_apk="$2"
            shift 2
            ;;
        --tgt-version)
            tgt_version="$2"
            shift 2
            ;;
        --tgt-repo-dir)
            tgt_repo_dir="$2"
            shift 2
            ;;
        --app-description)
            app_description="$2"
            shift 2
            ;;
        --model)
            model="$2"
            shift 2
            ;;
        --exact-model-name)
            exact_model_name="$2"
            shift 2
            ;;
        --system-prompt-file)
            system_prompt_file="$2"
            shift 2
            ;;
        --output-dir)
            output_dir="$2"
            shift 2
            ;;
        *)
            echo "Unknown argument: $1"
            exit 1
            ;;
    esac
done



package_name=$(bash scripts/get_package_name.sh "$ref_apk" | awk '{print $2}')
#ref_version=$(bash scripts/get_version_name.sh "$ref_apk")
#tgt_version=$(bash scripts/get_version_name.sh "$tgt_apk")

# 1. To run differencing and get intermediate result, run
diff_output_dir="${output_dir}/diff_results"
mkdir -p "$diff_output_dir"
echo "Running diffing for $package_name from $ref_version to $tgt_version"
java -jar Verlog-code-1.0-SNAPSHOT.jar \
    --android-jar="$android_sdk_path" \
    --output-dir="$diff_output_dir" \
    --package-name="$package_name" \
    --ref-apk="$ref_apk" \
    --ref-dir="$ref_repo_dir" \
    --ref-version="$ref_version" \
    --repo="$git_repo" \
    --tgt-apk="$tgt_apk" \
    --tgt-dir="$tgt_repo_dir" \
    --tgt-version="$tgt_version" &> /dev/null

# 2. To parse the intermediate result and generate prompts for summarization, run:
commit_messages_dir="${output_dir}/commit_messages"
commit_messages_dir_abs_path=$(realpath "$commit_messages_dir")
commit_messages_file="$commit_messages_dir_abs_path/$package_name-$ref_version-$tgt_version.txt"
mkdir -p "$commit_messages_dir"
(
    cd "$git_repo" || exit
    echo "Getting commit messages for $package_name from $ref_version to $tgt_version..."
    git log --pretty=format:"%s" "$ref_version".."$tgt_version" > "$commit_messages_file"
)

prompt_output_dir="${output_dir}/prompts"
mkdir -p "$prompt_output_dir"
echo "Generating prompts for $package_name from $ref_version to $tgt_version"
python3 verlog/verlog_summarizer/verlog.py parse \
    --input "$diff_output_dir/$package_name-$ref_version-$tgt_version-diff.json" \
    --ref-release-tag "$ref_version" \
    --tgt-release-tag "$tgt_version" \
    --repo-path "$git_repo" \
    --app-description "$app_description" \
    --commit-messages-file "$commit_messages_file" \
    --reduce_prompts \
    --output-dir "$prompt_output_dir"

# 3. To summarize and generate release note entries, run
rn_entries_dir="${output_dir}/rn_entries"
mkdir -p "$rn_entries_dir"
echo "Generating release note entries for $package_name from $ref_version to $tgt_version"
for prompt_file in "$prompt_output_dir"/*.prompt; do
    echo "Processing prompt file: $prompt_file"
    python3 verlog/verlog_summarizer/verlog.py summarize \
        --input-prompt-file "$prompt_file" \
        --model "$model" \
        --exact-model-name "$exact_model_name" \
        --system-prompt-file "$system_prompt_file" \
        --output-dir "$rn_entries_dir"
done


# 4. To synthesize and generate the release note, run
python3 verlog/verlog_summarizer/summarizer/synthesize_res.py \
    --input-dir "$rn_entries_dir" \
    --model "$model" \
    --exact-model-name "chat" \
    --commit-messages-file "$commit_messages_file" \
    --system-prompt-file "verlog/verlog_summarizer/assets/example_system_prompt_synthsize.txt" \
    --output-dir "$output_dir"


