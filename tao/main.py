import sqlite3
from pprint import pprint

import pystache
from scrapy.crawler import CrawlerProcess

import ui
import os
from tao.spiders.alibaba import AlibabaSpider


def dict_gen(curs):
    ''' From Python Essential Reference by David Beazley
    '''
    field_names = [d[0].lower() for d in curs.description]
    data = curs.fetchall()
    print(field_names)
    a = []
    for i in data:
        a.append(dict(zip(field_names, i)))
    return a


ui.show()
if not ui.config: exit(0)
pprint(ui.config)
pro = CrawlerProcess()
pro.crawl(AlibabaSpider, **ui.config)
pro.start()
conn = sqlite3.connect("haha.db")
cur = conn.cursor()
with open("tem.html", encoding='utf8') as f:
    tem = f.read()
res = cur.execute("SELECT * FROM tao ORDER BY 产品个数 DESC LIMIT 50")
data = dict_gen(res)
print(data)
s = pystache.render(tem, {
    'data': data,
    'title': ui.config['keyword'] + "&" + ui.config['keyword2']
})
with open("index.html", "w", encoding='utf8') as f:
    f.write(s)
cur.close()
conn.close()
os.startfile("index.html")
