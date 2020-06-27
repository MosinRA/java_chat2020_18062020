package server;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExService {
    /** Считаю, что в нашей реализации чата достаточно синг потока, в связи с низким функционалом, думаю если риализовывать
     * на многопоточность целесообразно кога будет более 100 т. пользователей и большое количество запросов на изменение/редактирования данных (история смена ника и т.д.)
     * конечно можно было сделать отдельный поток на запись истории...*/




    public void StartServer() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute((Runnable) new Server());

    }

}


