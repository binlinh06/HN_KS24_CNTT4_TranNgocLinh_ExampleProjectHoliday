const fs = require('fs');
const path = require('path');

const xmlPath = path.join(__dirname, 'srs_extracted', 'word', 'document.xml');
if (!fs.existsSync(xmlPath)) {
  console.error('word/document.xml not found!');
  process.exit(1);
}

const xml = fs.readFileSync(xmlPath, 'utf8');

// A simple regex to extract text in <w:t> tags
// Also handle paragraphs <w:p> to add newlines
// Let's parse tag by tag
let text = '';
let index = 0;

// Regular expression to match XML tags
const tagRegex = /<([^>]+)>/g;
let lastIndex = 0;
let match;

while ((match = tagRegex.exec(xml)) !== null) {
  const tag = match[1];
  const tagContent = xml.substring(lastIndex, match.index);
  
  // If it's a closing paragraph tag, add a newline
  if (tag === '/w:p' || tag === 'w:br/' || tag === 'w:cr/') {
    text += '\n';
  }
  
  // If the preceding tag was w:t, and there's text inside, add it
  // Wait, in XML, text content is between tags. Let's find w:t and extract its text.
  lastIndex = tagRegex.lastIndex;
}

// Let's do a more robust extraction using regex for w:t
// <w:t xml:space="preserve">text</w:t> or <w:t>text</w:t>
const wtRegex = /<w:t(?:[^>]*)>([^<]*)<\/w:t>/g;
let wtMatch;
const paragraphs = [];
let currentParagraph = '';

// Let's parse using a state machine or regex.
// A simpler way: split by <w:p> tags to keep paragraph structure.
const pParts = xml.split(/<w:p(?: [^>]*)?>/);
const textLines = [];

for (const p of pParts) {
  let pText = '';
  let m;
  const tRegex = /<w:t(?:[^>]*)>([^<]*)<\/w:t>/g;
  while ((m = tRegex.exec(p)) !== null) {
    pText += m[1];
  }
  if (pText.trim()) {
    textLines.push(pText.trim());
  }
}

const result = textLines.join('\n\n');
fs.writeFileSync(path.join(__dirname, 'srs_text.txt'), result, 'utf8');
console.log('Successfully extracted text from document.xml. Total lines: ' + textLines.length);
