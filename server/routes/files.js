const express = require('express');
const multer = require('multer');
const File = require('../models/File');
const router = express.Router();

const upload = multer({ dest: 'uploads/' });

router.post('/upload', upload.single('file'), async (req, res) => {
  try {
    const { filename, mimetype, size } = req.file;
    const { userId } = req.body;

    const file = await File.create({
      filename,
      mimetype,
      size,
      owner: userId
    });

    res.status(201).json(file);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

router.get('/files', async (req, res) => {
  try {
    const files = await File.find({ owner: req.user.id });
    res.json(files);
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

module.exports = router;