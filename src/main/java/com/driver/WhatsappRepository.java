package com.driver;

import java.util.*;

public class WhatsappRepository {
    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupuserMap;
    private HashMap<Group, List<Message>> groupmessMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;
    public WhatsappRepository(){
        this.groupmessMap = new HashMap<Group, List<Message>>();
        this.groupuserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        User user = new User(name, mobile);
        return "SUCCESS";
    }
    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group customGroupCount". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // If group is successfully created, return group.
        if(users.size()==2){
            Group group = new Group(users.get(1).getName(), 2);
            adminMap.put(group, users.get(0));
            groupuserMap.put(group, users);
            groupmessMap.put(group, new ArrayList<Message>());
            return group;
        }
        this.customGroupCount += 1;
        Group group = new Group(new String("Group "+this.customGroupCount), users.size());
        adminMap.put(group, users.get(0));
        groupuserMap.put(group, users);
        groupmessMap.put(group, new ArrayList<Message>());
        return group;
    }
    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        this.messageId += 1;
        Message message = new Message(messageId, content);
        return message.getId();
    }
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(adminMap.containsKey(group)){
            List<User> users = groupuserMap.get(group);
            Boolean userFound = false;
            for(User user: users){
                if(user.equals(sender)){
                    userFound = true;
                    break;
                }
            }
            if(userFound){
                senderMap.put(message, sender);
                List<Message> messages = groupmessMap.get(group);
                messages.add(message);
                groupmessMap.put(group, messages);
                return messages.size();
            }
            throw new Exception("You are not allowed to send message");
        }
        throw new Exception("Group does not exist");
    }
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".

        if(adminMap.containsKey(group)){
            if(adminMap.get(group).equals(approver)){
                List<User> participants = groupuserMap.get(group);
                Boolean userFound = false;
                for(User participant: participants){
                    if(participant.equals(user)){
                        userFound = true;
                        break;
                    }
                }
                if(userFound){
                    adminMap.put(group, user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a participant");
            }
            throw new Exception("Approver does not have rights");
        }
        throw new Exception("Group does not exist");
    }
    public int removeUser(User user) throws Exception{
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        Boolean userFound = false;
        Group userGroup = null;
        for(Group group: groupuserMap.keySet()){
            List<User> participants = groupuserMap.get(group);
            for(User participant: participants){
                if(participant.equals(user)){
                    if(adminMap.get(group).equals(user)){
                        throw new Exception("Cannot remove admin");
                    }
                    userGroup = group;
                    userFound = true;
                    break;
                }
            }
            if(userFound){
                break;
            }
        }
        if(userFound){
            List<User> users = groupuserMap.get(userGroup);
            List<User> updatedUsers = new ArrayList<>();
            for(User participant: users){
                if(participant.equals(user))
                    continue;
                updatedUsers.add(participant);
            }
            groupuserMap.put(userGroup, updatedUsers);

            List<Message> messages = groupmessMap.get(userGroup);
            List<Message> updatedMessages = new ArrayList<>();
            for(Message message: messages){
                if(senderMap.get(message).equals(user))
                    continue;
                updatedMessages.add(message);
            }
            groupmessMap.put(userGroup, updatedMessages);

            HashMap<Message, User> updatedSenderMap = new HashMap<>();
            for(Message message: senderMap.keySet()){
                if(senderMap.get(message).equals(user))
                    continue;
                updatedSenderMap.put(message, senderMap.get(message));
            }
            senderMap = updatedSenderMap;
            return updatedUsers.size()+updatedMessages.size()+updatedSenderMap.size();
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        List<Message> messages = new ArrayList<>();
        for(Group group: groupmessMap.keySet()){
            messages.addAll(groupmessMap.get(group));
        }
        List<Message> filteredMessages = new ArrayList<>();
        for(Message message: messages){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                filteredMessages.add(message);
            }
        }
        if(filteredMessages.size() < K){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(filteredMessages, new Comparator<Message>(){
            public int compare(Message m1, Message m2){
                return m2.getTimestamp().compareTo(m1.getTimestamp());
            }
        });
        return filteredMessages.get(K-1).getContent();
    }
}
