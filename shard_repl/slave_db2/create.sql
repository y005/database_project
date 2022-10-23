-- change master to master_host='192.168.0.4', master_user='samho101', master_password='1234', master_log_file='mysql-bin.000005', master_log_pos=300;
stop slave;
start slave;