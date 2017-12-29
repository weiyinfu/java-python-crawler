import sqlite3
import sys
import os

sys.path.append(os.path.abspath("./sqlite"))
conn = sqlite3.connect("haha.db")
cur = conn.cursor()
cur.execute("drop table IF EXISTS company")
cur.execute("""
CREATE TABLE company(
name VARCHAR(20),
cnt INT
)
""")
cur.execute("INSERT  INTO company VALUES('weidiao',24)")
conn.commit()
res = cur.execute("SELECT * FROM company")
print(res.fetchall())
cur.close()
conn.close()
