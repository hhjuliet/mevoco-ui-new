package zstackui

import grails.transaction.Transactional

interface ApiService {

    def void onSuccess(Boolean response);
    def void onError();
}
