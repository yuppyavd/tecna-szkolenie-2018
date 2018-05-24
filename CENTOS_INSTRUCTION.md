# Instrukcja instalacji i konfiguracji systemu CentOS


## Instalacja systemu w środowisku wirtualizacji Oracle VirtualBox

Ustawienia konfiguracyjne (pamięć, dysk) zostawić domyślne.

Ustawić hasło root. Dodać konto użytkownika z uprawnieniami administratora (najlepiej z mało złożonym hasłem oraz innym niż hasło dla root).

Zmienić typ karty sieciowej na "bridged".

Po instalacji zalogować się do systemu na stworzone konto użytkownika.

## Konfiguracja sieci

Wyświetlenie urządzeń sieciowych:

    $ nmcli d

Uruchomienie konfiguratora urządzeń sieciowych:

    $ nmtui

Restart usług sieciowych:

    $ sudo systemctl restart network

Wyświetlenie przydzielonych adresów IP:

    $ ip a

Można sprawdzić ping z serwera host. Jeśli działa, można już połączyć się poprzez PuTTY.


### Konfiguracja zapisu daty i czasu w historii poleceń

Dodanie zmiennej środowiskowej do konfiguracji profilu użytkownika:

    $ echo 'export HISTTIMEFORMAT="%d/%m/%y %T "' >> ~/.bash_profile

Przeładowanie konfiguracji profilu użytkownika:

    $ source ~/.bash_profile
    
Weryfikacja działania:

    $ history

## Aktualizacja systemu

Instalacja aktualizacji:

    $ sudo yum update

## Konfiguracja klienta NTP

Instalacja usługi ntp z repozytorium YUM:

    $ sudo yum install ntp

Sprawdzenie poprawności ustawień strefy czasowej:

    $ timedatectl

Jeśli strefa nieprawidłowa, można ustawić komendą:

    $ timedatectl set-timezone Europe/Warsaw
    
Dodanie usługi ntp do autostart'u:

    $ sudo systemctl enable ntpd
    
Uruchomienie usługi ntp:

    $ sudo systemctl start ntpd
    
Weryfikacja poprawności ustawienia daty i czasu:

    $ date

## Konfiguracja dostępu przez SSH

Wyłączenie logowania przez SSH na konto root:

    $ sudo vi /etc/ssh/sshd_config

W pliku należy dodać wpis: `PermitRootLogin no`

Restart usługi SSH:

    $ sudo systemctl restart sshd

## Instalacja przydatnych narzędzi

Instalacja przeglądarki tekstowej:

    $ sudo yum install links

Wciśnięcie `g` powoduje wyświetlenie paska adresu.

Instalacja narzędzia do pobierania plików:

    $ sudo yum install wget
    
Instalacja narzędzia do wypakowywania plików ZIP:

    $ sudo yum install unzip

## Instalacja serwera Apache WWW

Instalacja serwera z repozytorium:

    $ sudo yum install httpd

Dodanie usługi do autostart'u:

    $ sudo systemctl enable httpd

Włączenie usługi:

    $ sudo systemctl start httpd

Sprawdzenie statusu usługi:

    $ sudo systemctl status httpd

Sprawdzenie działania usługi:

    $ links http://localhost

Z systemu host usługa nie działa z powodu firewall'a.

Odblokowanie portów na firewall'u:

    $ sudo firewall-cmd --permanent --add-port=80/tcp
    $ sudo firewall-cmd --permanent --add-port=443/tcp
    $ sudo firewall-cmd --reload

Można wyświetlić stronę testową w przeglądarce na systemie host: `http://{adres_ip_vm}`

## Konfiguracja certyfikatu SSL

Instalacja mod_ssl do serwera Apache:

    $ sudo yum install mod_ssl

Utworzenie katalogu na klucze prywatne:

    $ sudo mkdir /etc/ssl/private

Dostęp do katalogu z kluczami prywatnymi tylko dla użytkownika root:

    $ sudo chmod 700 /etc/ssl/private

Wygenerowanie klucza prywatnego i certyfikatu:

    $ sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /etc/ssl/private/apache-selfsigned.key -out /etc/ssl/certs/apache-selfsigned.crt

Konfiguracja wygenerowanych certyfikatów:

    $ sudo vi /etc/httpd/conf.d/ssl.conf

Wewenątrz sekcji <VirtualHost _default_:443> należy dodać wpisy:

    SSLCertificateFile /etc/ssl/certs/apache-selfsigned.crt
    SSLCertificateKeyFile /etc/ssl/private/apache-selfsigned.key

Sprawdzenie poprawności konfiguracji Apache:

    $ sudo apachectl configtest

Restart serwera Apache:

    $ sudo apachectl restart

## Instalacja serwera Apache Tomcat

Instalacja Tomcat'a z repozytorium YUM:

    $ sudo yum install tomcat tomcat-webapps tomcat-admin-webapps

Dodanie Tomcat'a do autostart'u:

    $ sudo systemctl enable tomcat

Uruchomienie serwera Tomcat:

    $ sudo systemctl start tomcat

Podgląd logów:

    $ sudo ls /var/log/tomcat
    $ sudo less /var/log/tomcat/catalina.{data}.log

Sprawdzenie działania usługi:

    $ links http://localhost:8080

Z systemu host nie zadziała, port zablokowany przez firewall'a. Tworzymy tunel przez Putty i wchodzimy na adres http://localhost:8080 z systemu host.

Dodanie użytkownika administratora serwera Tomcat:

    $ sudo vi /etc/tomcat/tomcat-users.xml

Odkomentować `<user username="admin" ... />` i zmienić hasło.

Restart serwera Tomcat:

    $ sudo systemctl restart tomcat

Przykładowe aplikacje (można otworzyć z systemu host):

    http://localhost:8080/examples/websocket/index.xhtml

## Konfiguracja Proxy AJP

Utworzenie pliku z konfiguracją proxy:

    $ sudo vi /etc/httpd/conf.d/proxy.conf

Zawartość:

    <VirtualHost *:80>
      ProxyPreserveHost On

      ProxyPass /examples ajp://127.0.0.1:8009/examples
      ProxyPassReverse /examples ajp://127.0.0.1:8009/examples
    </VirtualHost>

Sprawdzenie poprawności konfiguracji:

    $ sudo apachectl configtest

Przeładowanie konfiguracji Apache:

    $ sudo apachectl graceful

Z systemu host można wejść na adres:

    http://{adres_ip_vm}/examples/

## Instalacja bazy danych PostgreSQL

Instalacja bazy z repozytorium YUM:

    $ sudo yum install postgresql-server postgresql-contrib

Utworzenie klastra:

    $ sudo postgresql-setup initdb

Logowanie do bazy za pomocą loginu i hasła:

    $ sudo vi /var/lib/pgsql/data/pg_hba.conf

W poniższych linijkach zmieniamy metodę `ident` na `md5`.

    # IPv4 local connections
    host    all             all             127.0.0.1/32            md5
    # IPv6 local connections
    host    all             all             ::1/128                 md5

Przełączenie na użytkownika postgres:

    $ sudo -i -u postgres

Włączenie narzędzia psql:

    $ psql

Utworzenie użytkownika bazodanowego:

    postgres=# create user training with password 'training';

Utworzenie bazy danych:

    postgres=# create database training owner training;

Z systemu host połączenie przez pgAdmin'a nie zadziała, port zablokowany przez firewall'a. Tworzymy tunel przez Putty i łączymy się pod adres localhost.

## Wgrywanie aplikacji na serwer Tomcat

Pobranie dodatkowej biblioteki "javax.el-api":

    $ wget http://central.maven.org/maven2/org/glassfish/javax.el/3.0.0/javax.el-3.0.0.jar
    $ sudo mv javax.el-3.0.0.jar /usr/share/tomcat/lib

Restart serwera Tomcat:

    $ sudo systemctl restart tomcat

Instalujemy aplikację przez Tomcat Manager'a: http://localhost:8080/manager
Po wgraniu aplikacja powinna być widoczna pod adresem: http://localhost:8080/training

## Wykonywanie kopii zapasowych w bazie danych PostgreSQL

Przelogowanie na użytkownika postgres:

    $ sudo -i -u postgres

Zrzucenie dumpa do pliku:

    $ pg_dump -c training | gzip > /var/lib/pgsql/backups/training.sql.gz

Wgranie dumpa z pliku:

    $ gunzip -c /var/lib/pgsql/backups/training.sql.gz | psql training

Po wgraniu kopii bazy może być konieczny restart serwera Tomcat.

## Instalacja serwera Payara 5

Pobranie archiwum z plikami serwera:

    $ wget http://search.maven.org/remotecontent?filepath=fish/payara/distributions/payara/5.181/payara-5.181.zip -O payara-5.181.zip

Rozpakowanie archiwum z plikami serwera:

    $ sudo unzip payara-5.181.zip -d /opt

Dodanie grupy użytkowników payara:

    $ sudo groupadd --system payara

Dodanie użytkownika payara:

    $ sudo useradd --system --shell /bin/bash -g payara payara

Zmiana właściciela plików serwera:

    $ sudo chown -R payara:payara /opt/payara5

Przelogowanie się na użytkownika payara:

    $ sudo su payara

Usunięcie istniejących domen:

    $ /opt/payara5/bin/asadmin delete-domain domain1
    $ /opt/payara5/bin/asadmin delete-domain production

Utworzenie domeny "test":

    $ /opt/payara5/bin/asadmin create-domain test
    
Uruchomienie domeny "test":

    $ /opt/payara5/bin/asadmin start-domain test

Stworzyć tunel przez Putty na port 4848 i wejść pod adres http://localhost:4848 z systemu host. Zmienić port http na 8180 i stworzyć na niego tunel przez Putty. Aplikacja powinna być widoczna pod adresem http://localhost:8180/training

## Konfiguracja usługi systemowej dla serwera Payara

Wyłączenie domeny (z użytkownika payara):

    $ /opt/payara5/bin/asadmin stop-domain test

Utworzenie pliku usługi:

    $ sudo vi /etc/systemd/system/payara_test.service

Zawartość pliku:

```
[Unit]
Description=Payara Server - Domain 'test'
After=network.target remote-fs.target

[Service]
User=payara
WorkingDirectory=/opt/payara5/glassfish
Environment=PATH=/bin:/usr/bin:/usr/lib/java/bin
Type=oneshot
RemainAfterExit=yes
ExecStart=/opt/payara5/glassfish/bin/asadmin start-domain test
ExecReload=/opt/payara5/glassfish/bin/asadmin restart-domain test
ExecStop=/opt/payara5/glassfish/bin/asadmin stop-domain test
TimeoutStartSec=300
TimeoutStopSec=30

[Install]
WantedBy = multi-user.target
```

Uruchomienie serwera poprzez usługę:

    $ sudo systemctl start payara_test
    
Dodanie usługi serwera do autostart'u:

    $ sudo systemctl enable payara_test
