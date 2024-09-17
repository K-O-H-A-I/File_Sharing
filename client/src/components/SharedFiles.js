import React from 'react';

function SharedFiles({ files }) {
  const fileTypes = [
    { type: 'Documents', icon: '📄' },
    { type: 'Photos', icon: '🖼️' },
    { type: 'Movies', icon: '🎬' },
    { type: 'Other', icon: '📁' },
  ];

  return (
    <div className="shared-files">
      <h2>Real estate deals</h2>
      <p>10 members</p>
      <div className="file-stats">
        <span>All files: {files.length}</span>
        <span>My files: {files.filter(file => file.owner === currentUser.id).length}</span>
      </div>
      <div className="file-types">
        {fileTypes.map(type => (
          <div key={type.type} className="file-type">
            <span>{type.icon}</span>
            <span>{type.type}</span>
            <span>{files.filter(file => file.type === type.type).length} files</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default SharedFiles;