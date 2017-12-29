# -*- coding: utf-8 -*-
# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: http://doc.scrapy.org/en/latest/topics/item-pipeline.html
import sqlite3
import os
from tao.items import TaoItem


class TaoPipeline(object):
    def open_spider(self, spider):
        print("open spider")
        if os.path.exists("haha.db"):
            os.remove("haha.db")
        self.conn = sqlite3.connect("haha.db")
        self.cur = self.conn.cursor()
        s = "create table tao(%s)" % ",".join(map(lambda x: x + " varchar(100) ", TaoItem.fields))
        self.cur.execute(s)

    def process_item(self, item, spider):
        k = list(TaoItem.fields.keys())
        v = list(map(item.get, k))
        s = []
        for i in range(len(v)):
            if v[i] and "'" in v[i]:
                v[i].replace("'", '')
            if v[i]:
                s.append("'%s'" % v[i])
            else:
                s.append("null")
        s = "insert into tao (%s)values(%s)" % (",".join(k), ','.join(s))
        self.cur.execute(s)
        self.conn.commit()
        return item

    def close_spider(self, spider):
        print("close spider")
        self.conn.commit()
        self.cur.close()
        self.conn.close()
