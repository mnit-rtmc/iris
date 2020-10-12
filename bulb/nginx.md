Setting up nginx with SELinux:

```
semanage port -a -t http_port_t -p tcp 3030

mkdir /usr/share/nginx/cache
chown nginx.nginx /usr/share/nginx/cache
chcon system_u:object_r:httpd_sys_content_t:s0 /usr/share/nginx/cache
semanage fcontext -a -t httpd_sys_rw_content_t /usr/share/nginx/cache
restorecon -v /usr/share/nginx/cache
```
