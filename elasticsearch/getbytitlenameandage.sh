curl -XPOST 'http://127.0.0.1:9200/myindex/_search?pretty' -d '{ "query": { "bool": { "must": [ { "match": { "title": "eggs" }}, { "nested": { "path": "comments", "query": { "bool": {"must": [{ "match": { "comments.name": "john" }},{ "match": { "comments.age":  28     }}]}}}}]}}}'