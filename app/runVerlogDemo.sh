#!/bin/bash

set -euo pipefail

version_pairs=(
    v1.10.1-v1.11.0
    v1.11.0-v1.11.1
    v1.11.1-v1.11.2
    v1.11.2-v1.11.3 
)

package_name="com.fmsys.snapdrop"
android_sdk_path="$HOME/android-platforms"
if [ ! -d "$android_sdk_path" ]; then
    echo "Android SDK path not found: $android_sdk_path"
    exit 1
fi
git_repo="demo-app/com.fmsys.snapdrop/git_repos"
app_description="Transfer files seamlessly between all your devices - Snapdrop."

model="DeepSeek"
exact_model_name="chat"
system_prompt_file="verlog/verlog_summarizer/assets/system_prompt_with_example_exemplers_2.txt"

for version_pair in "${version_pairs[@]}"; do
    IFS='-' read -r ref_version tgt_version <<< "$version_pair"
    echo "Running Verlog for $ref_version to $tgt_version"

    ref_apk="demo-app/com.fmsys.snapdrop/built_apks/com.fmsys.snapdrop-$ref_version.apk"
    ref_repo_dir="demo-app/com.fmsys.snapdrop/tagged_repos/$ref_version/com.fmsys.snapdrop-$ref_version"
    tgt_apk="demo-app/com.fmsys.snapdrop/built_apks/com.fmsys.snapdrop-$tgt_version.apk"
    tgt_repo_dir="demo-app/com.fmsys.snapdrop/tagged_repos/$tgt_version/com.fmsys.snapdrop-$tgt_version"

    bash verlog.sh \
        --android-sdk-path "$android_sdk_path" \
        --git-repo "$git_repo" \
        --ref-apk "$ref_apk" \
        --ref-version "$ref_version" \
        --ref-repo-dir "$ref_repo_dir" \
        --tgt-apk "$tgt_apk" \
        --tgt-version "$tgt_version" \
        --tgt-repo-dir "$tgt_repo_dir" \
        --app-description "$app_description" \
        --model "$model" \
        --exact-model-name "$exact_model_name" \
        --system-prompt-file "$system_prompt_file" \
        --output-dir "out/$package_name/$ref_version-$tgt_version"
    
done