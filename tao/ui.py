from tkinter import *

config = None
root = Tk()
root.title("阿里巴巴供应商产品种类数统计")
label_keyword = Label(root, text="搜索公司关键字")
entry_keyword = Entry(root)
label_keyword2 = Label(root, text="店内搜索关键字")
entry_keyword2 = Entry(root)
now_row = 0
label_keyword.grid(row=now_row, column=0)
entry_keyword.grid(row=now_row, column=1)
now_row += 1
label_keyword2.grid(row=now_row, column=0)
entry_keyword2.grid(row=now_row, column=1)
now_row += 1
frame_dis = LabelFrame(root, text="设置距离")
label_lanti = Label(frame_dis, text="纬度")
entry_lanti = Entry(frame_dis)
label_longi = Label(frame_dis, text="经度")
entry_longi = Entry(frame_dis)
label_dis = Label(frame_dis, text="距离")
dis = StringVar(frame_dis, "unlimited")
for i, j in enumerate((("50", "50公里"),
                       ("100", "100公里"),
                       ("200", "200公里"),
                       ("unlimited", "不限"))):
    radio_dis = Radiobutton(frame_dis, text=j[1], value=j[0], variable=dis)
    radio_dis.grid(row=1, column=i + 1)
frame_dis.grid(row=now_row, columnspan=4)
now_row += 1
label_lanti.grid(row=0, column=0)
entry_lanti.grid(row=0, column=1)
label_longi.grid(row=0, column=2)
entry_longi.grid(row=0, column=3)
label_dis.grid(row=1, column=0)
frame_manage_mode = LabelFrame(root, text="经营模式")
manage_mode_list = '生产加工 经销批发 招商代理 商业服务 以上全部'.split()
var_manage_mode = StringVar(frame_manage_mode, value=2)
for j, i in enumerate(manage_mode_list):
    radio_manage_mode = Radiobutton(frame_manage_mode, text=i, variable=var_manage_mode, value=j + 1)
    radio_manage_mode.grid(row=0, column=j)
frame_manage_mode.grid(row=now_row, columnspan=5, sticky="w")
now_row += 1


def run():
    global config
    config = {"keyword": entry_keyword.get(),
              "keyword2": entry_keyword2.get(),
              "longi": entry_longi.get(),
              "lati": entry_lanti.get(),
              "dis": dis.get(),
              "biztype": var_manage_mode.get()}
    for i in config:
        config[i] = config[i].strip()
    if config['dis'] == "unlimited": config['dis'] = ''
    if config['longi'] or config['lati'] or config['dis']:
        if not (config['longi'] and config['lati'] and config['dis']):
            label_message['text'] = "地理位置信息不完整"
            return
    if not config['keyword'] or not config['keyword2']:
        label_message["text"] = "关键字必须填写"
        return
    root.destroy()


now_row += 1
label_message = Label(root, fg='red')
label_message.grid(row=now_row, column=0, columnspan=4, sticky='w')
button_run = Button(root, text="开始", command=run, bg='grey', width=20)
button_run.grid(row=now_row, column=3, columnspan=2, sticky="e")


def show():
    root.mainloop()


if __name__ == '__main__':
    show()
    import time

    time.sleep(3)
