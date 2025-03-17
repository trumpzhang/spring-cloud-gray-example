import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 10,  // 20个虚拟用户
    duration: '20s',  // 持续5分钟
};

export default function () {
    http.get('http://192.168.57.10:31000/order/cpu/stress');
    // sleep(1);  // 每个请求之间暂停1秒
}