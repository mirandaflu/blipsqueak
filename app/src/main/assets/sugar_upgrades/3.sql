DELETE FROM squeak WHERE NOT EXISTS (SELECT * FROM blip WHERE id = squeak.blip_id);
DELETE FROM rule WHERE NOT EXISTS (SELECT * FROM blip WHERE id = rule.blip_id);