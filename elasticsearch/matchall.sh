curl -XGET 'http://127.0.0.1:9200/myindex/_search?pretty' -d '{ "query":{"match_all": {} }}'