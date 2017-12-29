# -*- coding: utf-8 -*-
import scrapy


class BaiduSpider(scrapy.Spider):
    name = 'cnblog'
    allowed_domains = ['cnblogs.com']
    start_urls = ['http://www.cnblogs.com/']

    def haha(self, response):
        print("haha============")

    def parse(self, response):
        print("parse==============")
        return scrapy.Request(response.url,dont_filter=False)
