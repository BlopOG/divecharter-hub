ALTER TABLE dive_sites ADD COLUMN image_url VARCHAR(255);

UPDATE dive_sites SET image_url = '/images/sites/blue-heron-bridge.jpg'    WHERE name = 'Blue Heron Bridge';
UPDATE dive_sites SET image_url = '/images/sites/molasses-reef.jpg'        WHERE name = 'Molasses Reef';
UPDATE dive_sites SET image_url = '/images/sites/palancar-gardens.jpg'     WHERE name = 'Palancar Gardens';
UPDATE dive_sites SET image_url = '/images/sites/ss-thistlegorm.jpg'       WHERE name = 'SS Thistlegorm';
UPDATE dive_sites SET image_url = '/images/sites/great-blue-hole.jpg'      WHERE name = 'Great Blue Hole';
UPDATE dive_sites SET image_url = '/images/sites/christ-of-the-abyss.jpg'  WHERE name = 'Christ of the Abyss';
UPDATE dive_sites SET image_url = '/images/sites/vandenberg-wreck.jpg'     WHERE name = 'Vandenberg Wreck';
UPDATE dive_sites SET image_url = '/images/sites/shark-point.jpg'          WHERE name = 'Shark Point';
UPDATE dive_sites SET image_url = '/images/sites/barracuda-point.jpg'      WHERE name = 'Barracuda Point';
UPDATE dive_sites SET image_url = '/images/sites/manta-ray-night-dive.jpg' WHERE name = 'Manta Ray Night Dive';