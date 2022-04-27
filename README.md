# clj-url-shortener
В проекте пока присутствуют лишние файлы, по умолчанию сгенерированные Luminus, некоторые из них я просто закомментил. 
Позже удалю все ненужные файлы и комментарии 


## Запуск проекта

1. Для запуска проекта в Intellij Idea нужно установить плагин cursive.
2. В открытом проекте в Idea пкм на project.clj -> Run REPL / Debug REPL
3. В репле вводим `(start)`, библиотека mount запустит все необходимые коннекты и создадст подобие pendency graph`а. 

## Запуск бд локально

1. Создать аккаунт на сайте datomic, скачать datomic starter https://www.datomic.com/get-datomic.html, разархивировать 
и открыть директорию в терминале
2. Запросить license key (Send licence key) в лк datomic https://my.datomic.com/account
3. Заменить license= в config/samples/dev-transactor-templates.properties на ключ, который пришел на почту

4. Запустить transactor - `bin/transactor -Ddatomic.printConnectionInfo=true config/samples/dev-transactor-template.properties` .
Дальнейшая работа происходит в отдельном окне терминала.
5. Если подключаемся впервые, то отдельном окне терминала создаем новую бд: для этого запускаем репл bin/repl , затем
`(require '[datomic.api :as d])`
`(def db-uri "datomic:dev://localhost:4334/url-shortener") `- где "url-shortener" - имя бд
`(d/create-database db-uri)` - создаем бд "url-shortener"
`(def conn (d/connect db-uri))` - проверяем соединение
6. Для работы с бд используется datomic client library, которая общается с Peer Server. Peer Server, в свою очередь 
обращается к хранилищу и транзактору для выполнения операций в бд. 
Запускаем Peer Server, myaccesskey и mysecret нужно заменить на url-shortener  :
`bin/run -m datomic.peer-server -h localhost -p 8998 -a url-shortener,url-shortener -d url-shortener,datomic:dev://localhost:4334/url-shortener `
7. Опционально в одтельном окне терминала можно запустить веб-консоль `bin/console -p 8080 dev datomic:dev://localhost:4334/`
8. В самом проекте запускаем репл и вводим `(start)`. В списке созданных зависимостей должен появиться
`"#'clj-url-shortener.db.core/conn"`




