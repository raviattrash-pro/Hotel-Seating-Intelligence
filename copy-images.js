import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const srcDir = 'C:\\Users\\ASUS\\.gemini\\antigravity\\brain\\627c2647-fac9-4137-a391-8dd2a2c6b6b2';
const destDir = path.join(__dirname, 'docs', 'images');

const images = [
  { src: 'floor_plan_mockup_1779678240714.png', dest: 'floor_plan_mockup.png' },
  { src: 'dashboard_mockup_1779678258683.png', dest: 'dashboard_mockup.png' },
  { src: 'table_placement_1779678486081.png', dest: 'table_placement.png' },
  { src: 'payment_screen_1779678508400.png', dest: 'payment_screen.png' },
  { src: 'welcome_pass_1779678525438.png', dest: 'welcome_pass.png' },
  { src: 'analytics_charts_1779678548745.png', dest: 'analytics_charts.png' }
];

try {
  if (!fs.existsSync(destDir)) {
    fs.mkdirSync(destDir, { recursive: true });
    console.log(`Created directory: ${destDir}`);
  }

  images.forEach(img => {
    const srcPath = path.join(srcDir, img.src);
    const destPath = path.join(destDir, img.dest);
    if (fs.existsSync(srcPath)) {
      fs.copyFileSync(srcPath, destPath);
      console.log(`Successfully copied ${img.src} -> ${img.dest}`);
    } else {
      console.warn(`Source file not found: ${srcPath}`);
    }
  });
} catch (err) {
  console.error('Error copying images:', err.message);
}
