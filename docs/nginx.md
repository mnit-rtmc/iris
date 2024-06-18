# Nginx

IRIS relies on the popular [nginx] web server for TLS termination.  This frees
IRIS from handling security-critical HTTPS communication for its [REST API].

IRIS has two nginx configuration files:
1. `nginx-iris.conf`: For HTTP (unencrypted) resources: Java client, XML,
   map tiles, mayfly
2. `nginx-rest.conf`: For HTTPS (encrypted) resources: Web client, REST API

The NGINX configuration file at `/etc/nginx/nginx.conf` needs to be edited
for this configuration.

In the first server block (listening on port 80), make this change:
```diff
     # Load configuration files for the default server block.
-    include /etc/nginx/default.d/*.conf;
+    include /etc/nginx/default.d/nginx-iris.conf;
```

The second server block (listening on port 443) needs to be enabled
(uncommented).  Also, a valid [certificate] must be created and stored at
`/etc/pki/nginx/server.crt`, with a private key at
`/etc/pki/nginx/private/server.key`.

Once everything is configured, restart nginx with:

```sh
systemctl restart nginx
```


[certificate]: https://letsencrypt.org/getting-started/
[nginx]: https://nginx.org/en/
[rest api]: rest_api.html
