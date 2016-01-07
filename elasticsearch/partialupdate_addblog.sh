script.inline: on

curl -XPOST localhost:9200/myindex/blogpost/1/_update -d '{
    "script" : "ctx._source.comments += new_comment",
    "params" : {
        "new_comment" : {"name": "Karin"}
    }
}'