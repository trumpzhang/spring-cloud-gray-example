kubectl set image deploy cloud-demo-user *=cloud-demo-user:v2.0 && \
 kubectl rollout pause deployment/cloud-demo-user