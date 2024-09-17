import React, { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import ProgressBar from './ProgressBar';

function FileUpload({ onFileUpload }) {
  const [uploadProgress, setUploadProgress] = useState(0);

  const onDrop = useCallback((acceptedFiles) => {
    const file = acceptedFiles[0];
    uploadFile(file);
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  const uploadFile = async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/api/upload', {
        method: 'POST',
        body: formData,
        onUploadProgress: (progressEvent) => {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(progress);
        },
      });

      if (response.ok) {
        const uploadedFile = await response.json();
        onFileUpload(uploadedFile);
        setUploadProgress(0);
      }
    } catch (error) {
      console.error('Error uploading file:', error);
    }
  };

  return (
    <div {...getRootProps()} className="file-upload">
      <input {...getInputProps()} />
      {isDragActive ? (
        <p>Drop the file here ...</p>
      ) : (
        <div>
          <h2>Upload your file</h2>
          <p>to the Real estate deals group</p>
        </div>
      )}
      {uploadProgress > 0 && <ProgressBar progress={uploadProgress} />}
    </div>
  );
}

export default FileUpload;