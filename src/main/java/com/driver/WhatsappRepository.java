package com.driver;

import java.util.*;

public class WhatsappRepository {
    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.

    private HashMap<String, User> userHashMap;

    //group db
    private HashMap<Group, List<User>> groupHashMap;

    //message db
    private HashMap<Integer, Message> messages;
    //private List<Message> messages;

    //group Messages db
    private HashMap<Group, List<Message>> groupMessage;

    //user Messages db
    private HashMap<User, List<Message>> userMessage;

    //group count
    private int groupCount=0;

    //Message count
    private int messageCount=0;

    public WhatsappRepository() {
        this.userHashMap = new HashMap<>();
        this.groupHashMap = new HashMap<>();
        this.messages = new HashMap<>();
        this.groupMessage = new HashMap<>();
        this.userMessage = new HashMap<>();
    }

    public String createUser(String name, String mobile) throws Exception{
        //exception
        User user = new User(mobile, name);
        userHashMap.put(mobile,user);
        return "SUCCESS";
    }
    public int createMessage(String content){
        messageCount++;
        Message message = new Message(messageCount, content);
        message.setTimestamp(new Date());
        messages.put(messageCount,message);
        return messageCount;
    }
    public Group createGroup(List<User> users){
        if(users.size()==2){
            Group group = new Group(users.get(1).getName(),2); //or get(0)
            groupHashMap.put(group,users);
            return group;
        }
        else{
            groupCount++;
            Group group = new Group("Group "+groupCount, users.size());
            groupHashMap.put(group, users);
            return group;
        }
    }

    public int sendMessageInGroup(Message message, User sender, Group group) throws Exception{
        if(!groupHashMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(!groupHashMap.get(group).contains(sender)){
            throw new Exception("You are not allowed to send message");
        }

        if(!groupMessage.containsKey(group)){
            List<Message> messages1 = new ArrayList<>();
            messages1.add(message);
            groupMessage.put(group, messages1);
        }
        else{
            groupMessage.get(group).add(message);
        }

        if(!userMessage.containsKey(sender)){
            List<Message> messages1 = new ArrayList<>();
            messages1.add(message);
            userMessage.put(sender, messages1);
        }
        else{
            userMessage.get(sender).add(message);
        }
        return groupMessage.get(group).size();
    }

    public int removeUser(User user) throws Exception{
        boolean userExist = false;
        boolean isAdmin = false;
        Group groupName = null;
        for(Group group:groupHashMap.keySet()){
            int num = 0;
            for(User user1:groupHashMap.get(group)){
                num++;
                if(user1.equals(user)){
                    if(num==1){
                        isAdmin=true;
                    }
                    userExist=true;
                    groupName = group;
                    break;
                }
            }
            if(userExist){
                break;
            }
        }
        if(!userExist){
            throw new Exception("User not found");
        }
        if(isAdmin){
            throw new Exception("Cannot remove admin");
        }

        List<Message> userMessages=userMessage.get(user);

        for(Message message: userMessages){
            messages.remove(message.getId());
            groupMessage.get(groupName).remove(message);
        }



        groupHashMap.get(groupName).remove(user);

        userMessages.remove(user);

        return groupHashMap.get(groupName).size()+groupMessage.get(groupName).size()+messages.size();

    }
    public String changeAdmin(User approver, User user, Group group)throws Exception{

        if(!groupHashMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(!approver.equals(groupHashMap.get(group).get(0))){
            throw new Exception("Approver does not have rights");
        }
        boolean check=false;
        for(User user1 : groupHashMap.get(group)){
            if(user1.equals(user)){
                check=true;
            }
        }

        if(!check){
            throw new Exception("User is not a participant");
        }

        User oldAdmin = groupHashMap.get(group).get(0);
        groupHashMap.get(group).set(0, user);
        groupHashMap.get(group).add(oldAdmin);

        return "SUCCESS";
    }
    public String findMessage(Date start, Date end, int K) throws Exception{

        boolean latest = false;
        int k=0;
        String message = null;
        for(int i=messages.size()-1; i>=0; i--){
            Message messages1 = messages.get(i);
            if(messages1.getTimestamp().compareTo(start)>0 && messages1.getTimestamp().compareTo(end)<0){
                k++;
                if(k==1){
                    latest=true;
                }
                if(latest) {
                    message = messages1.getContent();
                    latest = false;
                }
            }
        }
        if(k<K){
            throw new Exception("K is greater than the number of messages");
        }
        return message;    }
}
