import re

import requests
import scrapy
from pyquery import PyQuery as pq
from requests.models import PreparedRequest
from tao.items import TaoItem


class AlibabaSpider(scrapy.Spider):
    name = "alibaba"
    allowed_domains = ["1688.com"]
    start_urls = ['http://www.1688.com/']
    custom_settings = {
        'REDIRECT_MAX_TIMES': 0,
        "ITEM_PIPELINES": {
            'tao.pipelines.TaoPipeline': 100
        }
    }

    def __init__(self, longi=None,
                 lati=None,
                 dis=None,
                 biztype=None,
                 keyword="羽绒服女",
                 keyword2="羽绒服"):
        scrapy.Spider.__init__(self)
        self.longi = longi
        self.biztype = biztype
        self.lati = lati
        self.keyword = keyword
        self.keyword2 = keyword2
        self.dis = dis
        print({
            'longi': longi,
            'lati': lati,
            'keyword': keyword,
            'keyword2': keyword2,
            'dis': dis
        })

    def start_requests(self):
        resp = requests.get(self.form_request(1))
        resp.encoding = 'gbk'
        print(resp.url)
        res = re.search('<span class="total-page">共(\d+)页</span>', resp.text)
        totalPage = int(res.group(1)) if res else 1
        for i in range(totalPage):
            yield scrapy.Request(self.form_request(i + 1),
                                 callback=self.parse_page,
                                 encoding='gbk')

    def form_request(self, beginPage):
        p = PreparedRequest()
        params = {
            "button_click": "top",
            "n": "y",
            "keywords": bytes(self.keyword, 'gbk'),
            "beginPage": beginPage
        }

        if self.longi:
            params.update(longi=self.longi, lati=self.lati, dis=self.dis)
        if self.biztype:
            params.update(biztype=self.biztype)
        p.prepare_url("https://s.1688.com/company/company_search.htm", params)
        return p.url

    def form_company_request(self, it):
        p = PreparedRequest()
        p.prepare_url(it['主页'] + "/page/offerlist.htm", params={
            "keywords": bytes(self.keyword2, 'gbk')
        })
        return p.url

    def parse_page(self, resp):
        html = pq(str(resp.body, 'gbk'))
        company_items = html(".company-list-item")
        for i in range(company_items.length):
            co = company_items.eq(i)
            it = TaoItem()
            a = co(".list-item-title .list-item-title-text")
            it['名称'] = a.text()
            page = a.attr("href")
            if '?' in page:
                page = page[:page.index("?")]
            it['主页'] = page
            field_names = co(".list-item-detail .detail-field-name")
            for i in range(field_names.length):
                field = field_names.eq(i)
                value = field.next()
                field = field.text().strip(':').strip()
                value = value.text().strip()
                if field in it.fields:
                    it[field] = value
            yield scrapy.Request(self.form_company_request(it),
                                 callback=self.parse_company,
                                 encoding='gbk',
                                 meta={
                                     'data': it
                                 })

    def parse_company(self, resp):
        it = resp.meta['data']
        res = re.search("共搜索到<em>\s*(\d+)\s*</span></em>个符合条件的产品", str(resp.body, 'gbk'))
        if not res:
            print("cannot find", resp.url)
            cnt = 0
        else:
            cnt = res.group(1)
        it['产品个数'] = cnt
        yield it
