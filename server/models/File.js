const mongoose = require('mongoose');

// Define File schema
const fileSchema = new mongoose.Schema({
    filename: {
        type: String,
        required: true,
        trim: true,
    },
    filepath: {
        type: String,
        required: true,
        trim: true,
    },
    size: {
        type: Number,
        required: true,
        min: [0, 'File size must be greater than 0 bytes'],
    },
    mimetype: {
        type: String,
        required: true,
        trim: true,
    },
    checksum: {
        type: String,  // Useful for verifying file integrity
        required: true,
    },
    owner: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
    uploadDate: {
        type: Date,
        default: Date.now,
    },
    isShared: {
        type: Boolean,
        default: false,  // Track whether the file is shared
    },
});

// Indexing for better query performance (e.g., searching by filename, owner)
fileSchema.index({ filename: 1, owner: 1 });

// Virtual field to return file's URL if stored locally or in cloud storage
fileSchema.virtual('fileUrl').get(function () {
    // Assuming the files are stored locally in an 'uploads' directory
    return `/uploads/${this.filepath}`;
});

// Export the File model
const File = mongoose.model('File', fileSchema);
module.exports = File;
