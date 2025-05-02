require('dotenv').config();
const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');
const mysql = require('mysql2/promise');
const crypto = require('crypto'); // Added for token generation

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true })); // Add this for form data
app.use('/public/images', express.static('public/images'));

// Database Configuration
const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

// Email Transporter Configuration
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.MAIL_USER,
    pass: process.env.MAIL_PASS
  }
});

// Helper function to send email
const sendEmail = async (options) => {
  try {
    await transporter.sendMail(options);
  } catch (error) {
    console.error('Error sending email:', error);
    throw error;
  }
};

// Spectacles Routes
app.get('/spectacles', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM spectacles');
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// Reservation Route
app.post('/reserver', async (req, res) => {
  try {
    const {
      nom,
      email,
      telephone,
      tickets,
      prix_total,
      titre_spectacle,
      date_spectacle,
      heure_spectacle,
      lieu_spectacle
    } = req.body;

    // Validate required fields
    if (!nom || !email || !tickets || !titre_spectacle) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    // Insert reservation
    const [result] = await pool.query(
      `INSERT INTO reservations 
      (nom, email, telephone, tickets, prix_total, titre_spectacle, date_spectacle, heure_spectacle, lieu_spectacle) 
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [nom, email, telephone, tickets, prix_total, titre_spectacle, date_spectacle, heure_spectacle, lieu_spectacle]
    );

    // Update available places
    await pool.query(
      'UPDATE spectacles SET place_dispo = place_dispo - ? WHERE titre = ?',
      [tickets, titre_spectacle]
    );

    // Send confirmation email
    const htmlTicketContent = `
      <div style="max-width: 600px; margin: auto; background: #1c1c1e; color: #ffffff; font-family: 'Segoe UI', sans-serif; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.3); overflow: hidden;">
    <div style="padding: 24px; border-bottom: 1px solid #333;">
      <h2 style="text-align: center; color: #42FF00; margin-bottom: 12px;">🎭 Confirmation de Réservation</h2>
      <p style="text-align: center; font-size: 16px; color: #aaa;">Merci pour votre achat ! Voici les détails de votre ticket :</p>
    </div>
    <div style="display: flex; flex-direction: row; padding: 24px;">
      <!-- Partie gauche -->
      <div style="flex: 1;">
        <p style="margin: 8px 0;"><strong style="color: #42FF00;">Spectacle :</strong> ${titre_spectacle}</p>
        <p style="margin: 8px 0;"><strong>Date :</strong> ${date_spectacle}</p>
        <p style="margin: 8px 0;"><strong>Heure :</strong> ${heure_spectacle}</p>
        <p style="margin: 8px 0;"><strong>Lieu :</strong> ${lieu_spectacle}</p>
        <p style="margin: 8px 0;"><strong>Nombre de tickets :</strong> ${tickets}</p>
      </div>
      <!-- Séparateur -->
      <div style="width: 1px; background: #444; margin: 0 20px;"></div>
      <!-- Partie droite -->
      <div style="flex: 1; text-align: center; align-self: center;">
        <p style="font-size: 14px; color: #aaa;">Montant Total</p>
        <p style="font-size: 28px; font-weight: bold; color: #42FF00;">${prix_total} DT</p>
      </div>
    </div>
    <div style="background: #2c2c2e; text-align: center; padding: 12px; font-size: 14px; color: #ccc;">
      Veuillez présenter ce ticket lors de l'entrée au spectacle.<br/>
      Contactez-nous en cas de problème : <a href="mailto:support@evenement.com" style="color: #42FF00;">support@evenement.com / 92172893</a>
    </div>
  </div>
    `;

    await sendEmail({
      from: process.env.MAIL_USER,
      to: email,
      subject: 'Reservation Confirmation',
      html: htmlTicketContent
    });

    res.status(201).json({ message: 'Reservation successful' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Reservation error' });
  }
});

// Authentication Routes
app.post('/auth/register', async (req, res) => {
  try {
    const { username, email, password, phone } = req.body;

    // Validate input
    if (!username || !email || !password) {
      return res.status(400).json({ message: "Missing required fields" });
    }

    // Check if user exists
    const [users] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
    if (users.length > 0) {
      return res.status(400).json({ message: "User already exists" });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    // Create user
    const [result] = await pool.query(
      'INSERT INTO users (username, email, password_hash, phone) VALUES (?, ?, ?, ?)',
      [username, email, hashedPassword, phone]
    );

    // Generate JWT token
    const token = jwt.sign(
      { user: { id: result.insertId } },
      process.env.JWT_SECRET,
      { expiresIn: '5h' }
    );

    res.json({ token });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

app.post('/auth/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    // Validate input
    if (!email || !password) {
      return res.status(400).json({ message: "Email and password are required" });
    }

    // Find user
    const [users] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
    if (users.length === 0) {
      return res.status(400).json({ message: "Invalid credentials" });
    }

    const user = users[0];

    // Verify password
    const isMatch = await bcrypt.compare(password, user.password_hash);
    if (!isMatch) {
      return res.status(400).json({ message: "Invalid credentials" });
    }

    // Generate JWT token
    const token = jwt.sign(
      { user: { id: user.id } },
      process.env.JWT_SECRET,
      { expiresIn: '5h' }
    );

    res.json({ token });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
});

// Authentication Middleware
const authenticate = async (req, res, next) => {
  const authHeader = req.header('Authorization');
  
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ 
      error: 'Authentication required',
      details: 'Expected Authorization: Bearer <token> format'
    });
  }

  const token = authHeader.split(' ')[1];
  
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    
    const [users] = await pool.query('SELECT * FROM users WHERE id = ?', [decoded.user.id]);
    
    if (users.length === 0) {
      throw new Error('User not found');
    }

    req.user = users[0];
    next();
  } catch (err) {
    console.error('Authentication error:', err);
    res.status(401).json({ 
      error: 'Invalid authentication',
      details: err.message
    });
  }
};

// Password Reset Routes
// In your /auth/forgot-password endpoint
app.post('/auth/forgot-password', async (req, res) => {
    try {
      const { email } = req.body;
  
      if (!email) {
        return res.status(400).json({ error: 'Email is required' });
      }
  
      // Check if user exists
      const [users] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
      if (users.length === 0) {
        return res.status(404).json({ 
          success: false,
          message: "No account found with this email"
        });
      }
  
      // Generate 6-digit verification code
      const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();
      const resetTokenExpiry = new Date(Date.now() + 3600000); // 1 hour from now
  
      try {
        // Update user with verification code
        await pool.query(
          'UPDATE users SET reset_token = ?, reset_expires = ? WHERE email = ?',
          [verificationCode, resetTokenExpiry, email]
        );
      } catch (error) {
        if (error.code === 'ER_BAD_FIELD_ERROR') {
          // If columns don't exist, create them and try again
          await pool.query(`
            ALTER TABLE users 
            ADD COLUMN IF NOT EXISTS reset_token VARCHAR(255) NULL,
            ADD COLUMN IF NOT EXISTS reset_expires DATETIME NULL
          `);
          
          await pool.query(
            'UPDATE users SET reset_token = ?, reset_expires = ? WHERE email = ?',
            [verificationCode, resetTokenExpiry, email]
          );
        } else {
          throw error;
        }
      }
  
      // Send email with verification code
      await sendEmail({
        to: email,
        subject: 'Code de réinitialisation de mot de passe',
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #333;">Réinitialisation de votre mot de passe</h2>
            <p>Vous avez demandé à réinitialiser votre mot de passe. Voici votre code de vérification :</p>
            <div style="background: #f4f4f4; padding: 10px; margin: 20px 0; text-align: center; font-size: 24px; font-weight: bold;">
              ${verificationCode}
            </div>
            <p>Ce code est valable pendant 1 heure. Ne le partagez avec personne.</p>
            <p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>
          </div>
        `
      });
  
      res.json({ 
        success: true,
        message: "Verification code sent to email",
        code: verificationCode // Only for development/testing, remove in production
      });
      
    } catch (error) {
      console.error(error);
      res.status(500).json({ 
        success: false,
        message: "Server error"
      });
    }
});

app.post('/auth/verify-reset-code', async (req, res) => {
    try {
        const { email, code } = req.body;

        if (!email || !code) {
            return res.status(400).json({ error: 'Email and code are required' });
        }

        // Check if code matches
        const [users] = await pool.query(
            'SELECT * FROM users WHERE email = ? AND reset_token = ? AND reset_expires > NOW()',
            [email, code]
        );

        if (users.length === 0) {
            return res.status(400).json({ 
                success: false,
                message: "Code invalide ou expiré" 
            });
        }

        res.json({ 
            success: true,
            message: "Code vérifié avec succès"
        });

    } catch (error) {
        console.error(error);
        res.status(500).json({ 
            success: false,
            message: "Server error"
        });
    }
});

app.post('/auth/reset-password', async (req, res) => {
    try {
        const { email, newPassword } = req.body;
        
        // Vérification des données
        if (!email || !newPassword) {
            return res.status(400).json({ 
                success: false,
                message: "Email et nouveau mot de passe requis" 
            });
        }

        // Hash du nouveau mot de passe
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(newPassword, salt);

        // Mise à jour en base de données
        const [result] = await pool.query(
            'UPDATE users SET password_hash = ?, reset_token = NULL, reset_expires = NULL WHERE email = ?',
            [hashedPassword, email]
        );

        res.json({ 
            success: true,
            message: "Mot de passe mis à jour avec succès" 
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ 
            success: false,
            message: "Erreur serveur" 
        });
    }
});

// Protected Profile Route
app.get('/profile', authenticate, (req, res) => {
  const { id, username, email, phone } = req.user;
  res.json({ id, username, email, phone });
});

// Update Profile Route
app.put('/api/users/profile', authenticate, async (req, res) => {
  try {
    const { username, email, phone, currentPassword, newPassword } = req.body;
    const userId = req.user.id;

    // Get current user
    const [users] = await pool.query('SELECT * FROM users WHERE id = ?', [userId]);
    if (users.length === 0) {
      return res.status(404).json({ message: "User not found" });
    }

    const user = users[0];

    // Handle password change
    let hashedNewPassword = user.password_hash;
    if (newPassword) {
      if (!currentPassword) {
        return res.status(400).json({ message: "Current password is required" });
      }

      const isMatch = await bcrypt.compare(currentPassword, user.password_hash);
      if (!isMatch) {
        return res.status(400).json({ message: "Current password is incorrect" });
      }

      const salt = await bcrypt.genSalt(10);
      hashedNewPassword = await bcrypt.hash(newPassword, salt);
    }

    // Update user
    await pool.query(
      'UPDATE users SET username = ?, email = ?, phone = ?, password_hash = ? WHERE id = ?',
      [username || user.username, 
       email || user.email, 
       phone || user.phone, 
       hashedNewPassword, 
       userId]
    );

    // Return updated user (without password)
    const [updatedUsers] = await pool.query(
      'SELECT id, username, email, phone FROM users WHERE id = ?', 
      [userId]
    );
    
    res.json(updatedUsers[0]);

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
});

// Start Server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`✅ Server running on http://localhost:${PORT}`);
});