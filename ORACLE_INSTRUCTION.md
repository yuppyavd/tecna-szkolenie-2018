# Instrukcja instalacji i konfiguracji systemu zarządzania bazą danych ORACLE 11g XE dla CentOS

## Konfiguracja systemu CentOS pod SZBD Oracle 11g XE

Sprawdzenie aktualnego rozmiaru przestrzeni swap:

	$ sudo swapon -s

Sprawdzenie dostępności miejsca w systemie:

	$ df -h
	
Utworzenie pliku o rozmiarze 2GB pod przestrzeń pamięci swap:

	$ sudo dd if=/dev/zero of=/swapfile bs=2G count=1

Przygotowanie pliku do użycia:

	$ sudo chmod 600 /swapfile
	$ sudo mkswap /swapfile

Dodanie pliku pod przestrzeń pamięci swap:

	$ sudo swapon /swapfile
	
Dodanie pliku do konfiguracji pamięci systemu:

	$ /swapfile   none    swap    sw    0   0

Konfiguracja pliku hosts poprzez dodanie linijki:

	$ [ADRES_IP_MASZYNY] [ADRES_URL_MASZYNY] [NAZWA_MASZYNY]

Instalacja unzip:

	$ yum install unzip

Wyłączenie firewalla:

	$ systemctl stop firewalld

## Instalacja SZBD Oracle 11g XE

Pobranie plików instalacyjnych SZBD Oracle 11g XE:

	$ wget https://system.aurea-bpm.com/download/training/oracle-xe-11.2.0-1.0.x86_64.rpm.zip

Rozpakowanie plików instalacyjnych:

	$ unzip -q oracle-xe-11.2.0-1.0.x86_64.rpm.zip

Przejście do katalogu z plikami instalacji:

	$ cd Disk1

Instalacja SZBD Oracle 11g XE:

	$ rpm -i oracle-xe-11.2.0-1.0.x86_64.rpm

## Konfiguracja SZBD Oracle 11g XE

Uruchomienie konfiguratora SZBD:

	$ /etc/init.d/oracle-xe configure 

Przejście do plików SZBD:

	$ cd /u01/app/oracle/product/11.2.0/xe/bin

Uruchomienie skryptu konfiguracyjnego:

	$ . /u01/app/oracle/product/11.2.0/xe/bin/oracle_env.sh
	
Sprawdzenie możliwości zalogowania:

	$ sqlplus sys as sysdba

Konfiguracja pliku listenera (listener.ora):

	$ # listener.ora Network Configuration File:
	$ LISTENER =
	$ (DESCRIPTION_LIST =
	$ (DESCRIPTION =
	$ (ADDRESS = (PROTOCOL = IPC)(KEY = EXTPROC_FOR_XE))
	$ (ADDRESS = (PROTOCOL = TCP)(HOST = [URL_HOSTA])(PORT = 1521))
	$ )
	$ )
	$ ADR_BASE_LISTENER = /u01/app/oracle

Konfiguracja pliku nazw TNS (tnsnames.ora):

	$ # tnsnames.ora Network Configuration File:
	$ XE =
	$ (DESCRIPTION =
	$ (ADDRESS = (PROTOCOL = TCP)(HOST = [URL_HOSTA])(PORT = 1521))
	$ (CONNECT_DATA =
	$ (SERVER = DEDICATED)
	$ (SERVICE_NAME = XE)
	$ )
	$ )

Zrestartowanie SZBD Oracle 11g XE:

	$ systemctl stop oracle-xe
	$ systemctl start oracle-xe
	
Zrestartowanie usługi listnera:

	$ $ORACLE_HOME/bin/lsnrctl stop
	$ $ORACLE_HOME/bin/lsnrctl start
	$ $ORACLE_HOME/bin/lsnrctl status

## Konfiguracje środowiska i przestrzeni tabel SZBD Oracle 11g XE

Stworzenie przestrzeni tabel:

	$ CREATE TABLESPACE tbs_tecna
	$ DATAFILE 'tbs_tecna_01.dat' 
    $ SIZE 10M REUSE
    $ AUTOEXTEND ON NEXT 10M MAXSIZE 200M;
	
Stworzenie przestrzeni tymczasowej:

	$ CREATE TEMPORARY TABLESPACE tbs_temp_tecna
	$ TEMPFILE 'tbs_temp_tecna_01.dbf'
    $ SIZE 5M AUTOEXTEND ON;

Stworzenie nowego schematu:

	$ CREATE USER demo
	$ IDENTIFIED BY demo
	$ DEFAULT TABLESPACE tbs_tecna
	$ TEMPORARY TABLESPACE tbs_temp_tecna
	$ QUOTA 20M on tbs_tecna;

Nadanie uprawnień:

	$ GRANT create session TO demo;
	$ GRANT create table TO demo;
	$ GRANT create view TO demo;
	$ GRANT create any trigger TO demo;
	$ GRANT create any procedure TO demo;
	$ GRANT create sequence TO demo;
	$ GRANT create synonym TO demo;

Nazwy plików przestrzeni tabel:

	$ select tablespace_name, file_name from dba_data_files;

Wyłączenie instancji SZBD:

	$ shutdown immediate;

Zamontawnie instancji SZBD:

	$ startup mount;

Zmiana nazwy pliku danych:

	$ mv /u01/oradata/TEST/tbs_tecna.dbf /u01/oradata/TEST/tbs_demo.dbf

Rekonfiguracji przestrzeni nazw w SZBD Oracle 11g XE:

	$ alter database rename file '/u01/oradata/TEST/tbs_tecna.dbf' to '/u01/oradata/TEST/tbs_demo.dbf';

Uruchomienie instancji SZBD:

	$ alter database open;
