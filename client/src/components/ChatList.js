import React from 'react';
import './ChatList.css';

const ChatList = ({ chats, onSelectChat }) => {
    return (
        <div className="chat-list">
            {chats.map((chat, index) => (
                <div 
                    key={index} 
                    className="chat-item" 
                    onClick={() => onSelectChat(chat)}
                >
                    <span className="chat-name">{chat.name}</span>
                    <span className="chat-last-message">{chat.lastMessage}</span>
                </div>
            ))}
        </div>
    );
};

export default ChatList;
