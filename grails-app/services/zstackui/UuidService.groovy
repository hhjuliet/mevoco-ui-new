package zstackui

class UuidService {

    static scope = "singleton"

    def uuid
	UuidService(){
		uuid = UUID.randomUUID().toString().replace("-", "")
	}

}
