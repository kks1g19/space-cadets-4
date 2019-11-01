class User {
    private String username;
    private String contact;
    private String ip;
    private String passwordHash;
    private boolean isAdmin = false;

    public User(String username, String password, String machineKey){
        String AESKey = StringMerger.merge(machineKey, username);
        this.username = username;
        this.passwordHash = AES.encrypt(password, AESKey);
    }

    public User(String username, String password, String machineKey, boolean isAdmin){
        String AESKey = StringMerger.merge(machineKey, username);
        this.username = username;
        this.passwordHash = AES.encrypt(password, AESKey);
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin(){
        return this.isAdmin;
    }

    public boolean setAdmin(boolean isAdmin){
        this.isAdmin = isAdmin;
        return isAdmin;
    }

    public String getUsername(){
        return this.username;
    }

    public String getContact(){
        return this.contact;
    }

    public String getIp(){
        return this.ip;
    }

    public String getPassword(String decryptionKey){
        return AES.decrypt(this.passwordHash, decryptionKey);
    }

    public void setContact(String contact){
        this.contact = contact;
    }

    public String toString(){
        return "{\"username\":\"" + this.username + "\",\"contact\":\"" + this.contact + "\"}";
    }
}