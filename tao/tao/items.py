# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class TaoItem(scrapy.Item):
    名称 = scrapy.Field()
    主页 = scrapy.Field()
    主营产品 = scrapy.Field()
    所在地 = scrapy.Field()
    员工人数 = scrapy.Field()
    经营模式 = scrapy.Field()
    工艺类型 = scrapy.Field()
    加工方式 = scrapy.Field()
    厂房面积 = scrapy.Field()
    产品个数 = scrapy.Field()
    累计成交数 = scrapy.Field()
    累计买家数 = scrapy.Field()
    重复采购率 = scrapy.Field()


if __name__ == '__main__':
    x=TaoItem()
    print(TaoItem.fields)
