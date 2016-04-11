from starcluster.clustersetup import ClusterSetup
from starcluster.logger import log

class DmasonSetup(ClusterSetup):
     def __init__(self):
          log.debug('Running dmason init')
     
     def run(self, nodes, master, user, user_shell, volumes):
          self.master_ip = master.private_ip_address
          log.debug('Master ip %s ' % self.master_ip)
          #the param detach=True will result in adding nohup at the beining and "&" at the end of the string below
          master.ssh.execute(\
               " --version > /dev/null && cd /root/dmason/ && nohup /root/java/jdk1.8.0_73/bin/java -jar DMASON-3.1.jar -m master 1> master_out.log 2> master_err.log ", \
               detach=True, source_profile=False)

          log.debug('Sleeping 30s')
          import time
          time.sleep(30) 
          log.debug('Setting %s worker nodes ' % len(nodes))
          for node in nodes:
               #the param detach=True will result in adding nohup at the beining and "&" at the end of the string below
               node.ssh.execute(\
                    " --version > /dev/null && cd /root/dmason/ && nohup /root/java/jdk1.8.0_73/bin/java -jar DMASON-3.1.jar -m worker -ip %s -p 61616 -ns %s 1> worker_out.log 2> worker_err.log " % (self.master_ip, node.num_processors), \
                    detach=True, source_profile=False)
               log.debug("%s added." % node.private_dns_name)
          log.debug('Done.')