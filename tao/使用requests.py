import requests
from pyquery import PyQuery as pq
from flask import Flask
import re
import time
import json

sess = requests.session()


def search_company(keywords,
                   longi=None,
                   lati=None,
                   dis=None,
                   biztype=None,
                   beginPage=1):
    """
    :param keywords:查询关键词
    :param longi:经度
    :param lati:纬度
    :param dis: 100,200,300,unlimited
    :param biztype:生产加工、经销批发、招商代理、商业服务、经营模式
    :return:
    """
    url = "https://s.1688.com/company/company_search.htm?" + \
          "?button_click=top&" + \
          "n=y&" + \
          "keywords=" + requests.utils.quote(keywords, encoding='gbk') + \
          "&beginPage=" + str(beginPage)
    if longi:
        url += "&longi=" + str(longi) + \
               "&lati=" + str(lati) + \
               "&dis=" + str(dis)
    if biztype:
        url += "&biztype=" + str(biztype)
    resp = sess.get(url)
    print(resp.url)
    resp.encoding = 'gbk'
    html = pq(resp.text)
    company_items = html(".company-list-item")
    companies = []
    for i in range(company_items.length):
        co = company_items.eq(i)
        it = dict()
        a = co(".list-item-title .list-item-title-text")
        it['name'] = a.text()
        it['page'] = a.attr("href")
        if '?' in it['page']:
            it['page'] = it['page'][:it['page'].index("?")]
        companies.append(it)
    return companies


def search_all_company(keywords="羽绒服女",
                       longi="116.4075",
                       lati="39.904030",
                       dis="200",
                       biztype="2"):
    beginPage = 0
    a = []
    while 1:
        beginPage += 1
        com = search_company(keywords, longi, lati, dis, biztype, beginPage)
        if not com:
            break
        a.extend(com)
    return a


def get_product_cnt(company, keyword="羽绒服"):
    resp = sess.get(
        company['page'] + "/page/offerlist.htm?keywords=" + requests.utils.quote(self.keywords, encoding='gbk'))
    resp.encoding = 'gbk'
    print(resp.url)
    with open("haha.html", "w", encoding='gbk') as f:
        f.write(resp.text)
    res = re.search("共搜索到<em>\s*(\d+)\s*</span></em>个符合条件的产品", resp.text)
    cnt = res.group(1)
    company['product_cnt'] = cnt

    co = search_all_company()
    for i in co:
        # get_product_cnt(i, '羽绒服')
        print(i)
