import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 20,  // 20个虚拟用户
    duration: '1m',  // 持续5分钟
};

export default function () {
    http.get('http://192.168.57.10:31000/user/cpu/stress');
    // sleep(1);  // 每个请求之间暂停1秒
}