import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import ChatList from './components/ChatList';
import FileUpload from './components/FileUpload';
import SharedFiles from './components/SharedFiles';

function App() {
  const [user, setUser] = useState(null);
  const [files, setFiles] = useState([]);

  useEffect(() => {
    // Fetch user data and files from the server
    fetchUserData();
    fetchFiles();
  }, []);

  const fetchUserData = async () => {
    // Implement API call to get user data
  };

  const fetchFiles = async () => {
    // Implement API call to get shared files
  };

  return (
    <div className="app">
      <Sidebar user={user} />
      <div className="main-content">
        <ChatList />
        <FileUpload onFileUpload={(file) => setFiles([...files, file])} />
        <SharedFiles files={files} />
      </div>
    </div>
  );
}

export default App;