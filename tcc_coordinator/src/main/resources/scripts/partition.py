import ConfigParser
import MySQLdb
import calendar
import smtplib
from datetime import timedelta
from datetime import datetime

def sedmail(msg):
    #print msg
    mssg_body=msg
    smtpserver = '10.120.144.233'
    smtpport = 10025
    AUTHREQUIRED = 1
    smtpuser = 'ddb_hz@corp.netease.com'
    smtppass = 'ddb_hz|netease1'
    RECIPIENTS = []
    SENDER = 'ddb_hz@corp.netease.com'
    mssg = """From: ddb_hz <ddb_hz@corp.netease.com>
To: dba <dba@corp.netease.com>
Subject: Auto Add or Drop Partition , this script is used for RDS instance

%s
        """ % ( mssg_body)

    session = smtplib.SMTP(smtpserver, smtpport)
    if AUTHREQUIRED:
        session.login(smtpuser, smtppass)
        smtpresult = session.sendmail(SENDER, RECIPIENTS, mssg)
        session.quit()

def execusql (hostname , port , user , password , database ,  sql ) :
    conn = MySQLdb.connect(host=hostname,port=port,user=user,passwd=password)
    curs = conn.cursor()
    conn.select_db(database)
    curs.execute(sql)
    conn.close()
    curs.close()

def  GetPartition (hostname , port , user , password , database ,  sql ) :
    partition_arrry=[]
    conn = MySQLdb.connect(host=hostname,port=port,user=user,passwd=password)
    curs = conn.cursor()
    conn.select_db(database)
    curs.execute(sql)
    result = curs.fetchall()
    conn.close ()
    curs.close ()
    for i in result :
        partition_arrry.append (i)
    return partition_arrry

def diffday (now  , minday) :
    now_day = now[6:8]
    now_month = now[4:6]
    now_year = now[0:4]
    min_day= minday[6:8]
    min_month= minday[4:6]
    min_year = minday[0:4]
    today= datetime.date(long(now_year),long(now_month),long(now_day))
    otherday= datetime.date(long(min_year),long(min_month),long(min_day))
    return (today-otherday).days

#get the partition count less than today
def leftpartition (partitions  , now , i) :
    num=0
    for partition in partitions :
        if long(partition[2]) <= now*long(i) :
            num=num+1
    return num
def extendpartition (partitions , now , i) :
    num=0
    for partition in partitions :
        if long(partition[2]) > now*long(i) :
            num=num+1
    return num

cnf = ConfigParser.ConfigParser()
cnf.read("partition.lst")
tables = cnf.sections()
msg=''
for i in tables :
    now=long(calendar.timegm(datetime.utcnow().utctimetuple()))
    host=cnf.get(i,'ip')
    port=cnf.get(i,'port')
    user=cnf.get(i,'user')
    password=cnf.get(i,'password')
    keep=cnf.get(i,'keeppartition')
    database=cnf.get(i,'database')
    table= cnf.get(i , 'tablename')
    extend= cnf.get(i , 'extendpartition')
    interval= cnf.get(i , 'interval')
    daytime= cnf.get (i ,'daytime')
    num=int(daytime) / 86400
    sql="select table_name , PARTITION_NAME , PARTITION_DESCRIPTION  from information_schema.PARTITIONS where table_name='%s' and TABLE_SCHEMA = '%s' order by  PARTITION_DESCRIPTION desc " % (table , database)
    partition_arrry= GetPartition(host , int(port) , user , password , database , sql  )
    max_value = long(partition_arrry[0][2])
    min_value = long(partition_arrry[len(partition_arrry)-1][2])
    max_date=datetime.fromtimestamp(max_value/num).strftime('%Y%m%d')
    min_date=datetime.fromtimestamp(min_value/num).strftime('%Y%m%d')


    msg=msg+'******************tablename is %s . database is %s . mysql host is %s . mysql port is %s ******************\n' % (table , database , host , port)
    left_par = leftpartition (partition_arrry , now , num)
    extend_par = extendpartition(partition_arrry  , now , num)

    diff_value = int(interval)*int(daytime)

    if ( extend_par < long(extend) ) :
        msg=msg+ "there are too little partition , so need add new  partitions \n"
        while (extend_par < long(extend) ) :
            time1= datetime.strptime(max_date,"%Y%m%d")
            time2= time1 + timedelta(days=int(interval))
            #print time1 , time2
            max_date=time2.strftime ('%Y%m%d')
            #print max_value , diff_value
            max_value=max_value+diff_value
            sql_command = 'alter table %s add PARTITION (PARTITION %s VALUES LESS THAN (%d) )' % (table , str('p'+max_date) , max_value)
            execusql (host , int(port) , user , password , database , sql_command)
            msg=msg+ sql_command +"\n"
            extend_par = extend_par +1
    else :
        msg=msg+ "there are much no used partitions , so there is no need to add more  partitions \n"

    if  left_par <= int (keep) :
         msg=msg+  "there is no need to drop old partition , there are only %d old partition , we must keep at least  %s  partitions , so must not drop partition \n" % (left_par , keep)
    else :
        while (left_par > int (keep)) :
            msg=msg+ "there are too much old partitions :%s , but we should keep %s partition , so we must drop the oldeset partition %s  \n" % (left_par,keep , partition_arrry[len(partition_arrry)-1][1])
            sql1="alter table %s drop partition %s \n" % (table , partition_arrry[len(partition_arrry)-1][1])
            msg=msg+sql1+"\n"
            execusql (host , int(port) , user , password , database , sql1)
            partition_arrry= GetPartition(host , int(port) , user , password , database , sql  )
            left_par = leftpartition (partition_arrry , now , num)
    msg=msg+'******************tablename is %s . database is %s . mysql host is %s . mysql port is %s ******************\n\n' % (table , database , host , port)
#sedmail (msg)
print msg
