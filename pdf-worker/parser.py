#!/usr/bin/env python
#-*- coding: utf-8 -*-
import requests, socket, os, argparse, json
import fitz
from collections import defaultdict

print("Начало работы pdf-worker.")

def extract_text_from_pdf(pdf_path):
    with fitz.open(pdf_path) as doc:
        text = ""
        for page in doc:
            text += page.getText().strip()
        return text


def make_data(text):
    data = {}
    if text == "":
        return data
    sep=''
    text2 = text.split()
    data['ogrn'] = ""
    data['lastName'] = ""
    data['firstName'] = ""
    data['patronymic'] = ""
    data['activities'] = []
    #print(text2)
    for i in range(0, len(text2)):
        if text2[i] == 'ОГРН' and text2[i] not in data:
            data['ogrn'] =text2[i+1]
        if text2[i] == 'Фамилия' and data['lastName'] == "":
            data['lastName'] = text2[i+3].capitalize()
        if text2[i] == 'Имя' and data['firstName'] == "":
            data['firstName'] = text2[i+3].capitalize()
        if text2[i] == 'Отчество' and data['patronymic'] == "":
            data['patronymic'] = text2[i+3].capitalize()
        if ' '.join(text2[i:i+5]) == "Сведения об основном виде деятельности":
            while  ' '.join(text2[i:i+5]) != 'Код и наименование вида деятельности':
                i+=1
            i+=5
            code = text2[i]
            i+=1
            activity = ''
            while not str.isnumeric(text2[i]) and text2[i] != 'Страница':
                activity += text2[i] + ' '
                i+=1
            activityEntry = defaultdict()
            activityEntry['activityCode'] = code
            activityEntry['description'] = activity.strip()
            activityEntry['isMain'] = True
            data['activities'].append(activityEntry)
        if ' '.join(text2[i:i+5]) == "Сведения о дополнительных видах деятельности" :
            while 'Сведения о записях, внесенных в ЕГРИП' not in ' '.join(text2[i:i+6])  and i < len(text2):
                if ' '.join(text2[i:i+5]) == 'Код и наименование вида деятельности' :
                    i+=5
                    code = text2[i]
                    i+=1
                    activity = ''
                    while not str.isnumeric(text2[i]) and text2[i] != 'Страница':
                        activity += text2[i] + ' '
                        i+=1
                    activityEntry = defaultdict()
                    activityEntry['activityCode'] = code
                    activityEntry['description'] = activity.strip()
                    activityEntry['isMain'] = False
                    data['activities'].append(activityEntry)
                i+=1

    return data


pdf_path = './doc.pdf'


parser = argparse.ArgumentParser()
parser.add_argument('ip', type=str, help='IP for socket to bind to. Required')
parser.add_argument('--bindPort', '-b', type=int, help='Port for socket to bind to. Default = 8880', nargs='?', default=8880)
parser.add_argument('--connPort', '-c', type=int, help='Port for socket to connect to web-server. Default = 8000', nargs='?', default=8000)
parser.add_argument('--token', '-t', type=str, help='Token for API access. Required', required=True)
args = parser.parse_args()

try:
    tcp_parser_socket = socket.socket()
    tcp_parser_socket.bind((args.ip, args.bindPort))
    udp_send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    udp_send_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    udp_send_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    data = f"NEW_WORKER;{args.ip};{args.bindPort}"
    udp_send_socket.sendto(data.encode('utf-8'), ('255.255.255.255', args.connPort))
    tcp_parser_socket.listen(1)
    tcp_parser_socket.settimeout(60)

    while True:
        conn, addr = tcp_parser_socket.accept()
        data = conn.recv(10240).decode('utf-8')
        if data == 'CREATE_CONNECTION':
            tcp_parser_socket.settimeout(None)
            break
        else:
            udp_send_socket.sendto(data.encode('utf-8'), ('255.255.255.255', args.connPort))

    udp_send_socket.close()

    while True:
        data = conn.recv(10240).decode('utf-8').strip()
        print(f'Получен запрос на обработку ОГРН: {data}')
        resp = requests.get(f"https://api-fns.ru/api/vyp?req={data}&key={args.token}")
        handle = open(pdf_path, 'wb')
        handle.write(resp.content)
        resp.close()
        handle.close()
        parsed_data_object = make_data(extract_text_from_pdf(pdf_path))
        parsed_data_message = json.dumps(parsed_data_object, ensure_ascii=False)
        print(parsed_data_message)
        os.remove(pdf_path)
        conn.send(parsed_data_message.encode('utf-8'))


except Exception as e:
    print(e)
finally:
    tcp_parser_socket.close()
    conn.close()
    handle.close()
    resp.close()
    if (os.path.isfile(pdf_path)):
        os.remove(pdf_path)
