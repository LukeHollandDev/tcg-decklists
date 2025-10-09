#!/bin/bash

# read in the metadata.json

# loop through each list item in the json

# extract the name (indicates data/<name> as location of the data)
# extract the source (provides GitHub url)
# extract the context (path within the GitHub repository to find the data)
# extract the version (provides most recently imported commit hash)
# use curl to get details about latest commit (extract username and repo from the source)
# curl https://api.github.com/repos/<username>/<repo>/commits?per_page=1 | jq '(.[0] | { sha: .sha, message: .commit.message, date: .commit.author.date })' 
# if the commit hash matches the one from the metadata, skip this loop
# if it they do not match then delete the data directory "data/<name from metadata>"
# clone the repository into that directory
# run the data/scripts/<name from metadata>-prepare.sh which prepares the data to be insertable easily into the database (it creates data/<name from metadata>/.out container csv files with names of the table)
# run the data/scripts/<name from metadata>-migrate.sh which will run the SQL to update the tables with the data from the update repository
# on failure: update the metadata for this specific name, set successful to false, the version to the latest hash from the query and timestamp to the date from the commit too
# on success: same as above but successful to true

# end loop

# check the diff to see if in the loop we updated any files in the data/ directory
# commit and push these changes if there is any

exit 0