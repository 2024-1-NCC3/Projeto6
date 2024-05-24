console.log("App online!");

var express = require("express");
var app = express();
const bodyParser = require("body-parser");
const bcrypt = require("bcrypt");
var port = process.env.PORT || 8080;
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
var sqlite3 = require("sqlite3").verbose();
const DBPATH = "appBD.db";
var db = new sqlite3.Database(DBPATH);

// Criar tabelas se não existirem
db.serialize(() => {
  db.run(`CREATE TABLE IF NOT EXISTS cadastro (
    idUser INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    email TEXT NOT NULL,
    senha TEXT NOT NULL
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS cadastro_ong (
    idOng INTEGER PRIMARY KEY AUTOINCREMENT,
    nome_ong TEXT NOT NULL,
    link TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    cnpj TEXT NOT NULL UNIQUE,
    descricao TEXT NOT NULL,
    telefone TEXT NOT NULL,
    ods TEXT NOT NULL,
    usuario_id INTEGER,
    FOREIGN KEY (usuario_id) REFERENCES cadastro (idUser)
  )`);

  // Verifica se a coluna imagemUri já existe antes de tentar adicioná-la
  db.all(`PRAGMA table_info(cadastro_ong)`, (err, columns) => {
    if (err) {
      console.error("Erro ao verificar colunas da tabela:", err);
    } else {
      const columnExists = columns.some(
        (column) => column.name === "imagemUri"
      );
      if (!columnExists) {
        db.run(`ALTER TABLE cadastro_ong ADD COLUMN imagemUri TEXT`, (err) => {
          if (err) {
            console.error("Erro ao adicionar coluna imagemUri:", err);
          } else {
            console.log("Coluna imagemUri adicionada com sucesso.");
          }
        });
      } else {
        console.log("Coluna imagemUri já existe.");
      }
    }
  });
});

app.get("/", function (req, res) {
  res.send("Tudo certo por aqui!");
});

// Endpoint para contar o total de ONGs
app.get("/ongs/count", function (req, res) {
  const qCount = `SELECT COUNT(*) AS count FROM cadastro_ong`;
  db.get(qCount, [], (err, row) => {
    if (err) {
      res.status(500).send("Erro interno do servidor: " + err);
    } else {
      res.json({ total: row.count });
    }
  });
});



app.post("/register", async function (req, res) {
  var nome = req.body.nome;
  var email = req.body.email;
  var senha = req.body.senha;

  const hashedPassword = await bcrypt.hash(senha, 10);

  const qSelect = `SELECT * FROM cadastro WHERE email='${email}'`;
  db.all(qSelect, [], (err, rows) => {
    if (err) {
      res.status(500).send("Erro interno do servidor: " + err);
    } else {
      if (rows.length > 0) {
        res.send("Usuário já existe!");
      } else {
        const qInsert = `INSERT INTO cadastro (nome, email, senha) VALUES ('${nome}', '${email}', '${hashedPassword}')`;
        db.run(qInsert, [], (err) => {
          if (err) {
            res.status(500).send("Erro interno do servidor: " + err);
          } else {
            res.send("Usuário cadastrado!");
          }
        });
      }
    }
  });
});

app.post("/login", function (req, res) {
  var email = req.body.email;
  var senha = req.body.senha;
  const qLogin = `SELECT * FROM cadastro WHERE email='${email}'`;
  db.all(qLogin, [], async (err, rows) => {
    if (err) {
      res.status(500).send("Erro interno do servidor: " + err);
    } else {
      if (rows.length === 0) {
        res.send("Email incorreto");
      } else {
        const validPassword = await bcrypt.compare(senha, rows[0].senha);
        if (validPassword) {
          res.send("Login realizado com sucesso");
        } else {
          res.send("Senha incorreta");
        }
      }
    }
  });
});

app.post("/register-ong", function (req, res) {
  var nome_ong = req.body.nome;
  var link = req.body.link;
  var latitude = req.body.latitude;
  var longitude = req.body.longitude;
  var cnpj = req.body.cnpj;
  var descricao = req.body.descricao;
  var telefone = req.body.telefone;
  var ods = req.body.ods;
  var imagemUri = req.body.imagemUri || "";

  if (
    !nome_ong ||
    !link ||
    !latitude ||
    !longitude ||
    !cnpj ||
    !descricao ||
    !telefone ||
    !ods
  ) {
    res.status(400).send("Todos os campos são obrigatórios");
    return;
  }

  const qSelect = `SELECT * FROM cadastro_ong WHERE cnpj='${cnpj}'`;
  db.all(qSelect, [], (err, rows) => {
    if (err) {
      res.status(500).send("Erro interno do servidor: " + err);
    } else {
      if (rows.length > 0) {
        res.send("ONG já existe!");
      } else {
        const qInsert = `INSERT INTO cadastro_ong (nome_ong, link, latitude, longitude, cnpj, descricao, telefone, ods, imagemUri) 
                         VALUES ('${nome_ong}', '${link}', ${latitude}, ${longitude}, '${cnpj}', '${descricao}', '${telefone}', '${ods}', '${imagemUri}')`;
        db.run(qInsert, [], (err) => {
          if (err) {
            res.status(500).send("Erro interno do servidor: " + err);
          } else {
            res.send("ONG cadastrada!");
          }
        });
      }
    }
  });
});

app.post("/login-ong", function (req, res) {
  var cnpj = req.body.cnpj;
  var descricao = req.body.descricao;
  const qLogin = `SELECT * FROM cadastro_ong WHERE cnpj='${cnpj}'`;
  db.all(qLogin, [], (err, rows) => {
    if (err) {
      res.status(500).send("Erro interno do servidor: " + err);
    } else {
      if (rows.length === 0) {
        res.send("CNPJ incorreto");
      } else {
        if (rows[0].descricao === descricao) {
          res.send("Login realizado com sucesso");
        } else {
          res.send("Descrição incorreta");
        }
      }
    }
  });
});

app.delete("/deleteUser", function (req, res) {
  var email = req.body.email;
  const sql = `DELETE FROM cadastro WHERE email='${email}'`;
  db.run(sql, [], (err) => {
    if (err) {
      res.status(500).send("Erro interno do servidor: " + err);
    } else {
      res.send("Usuário excluído!");
    }
  });
});

app.delete("/deleteOng", function (req, res) {
  var cnpj = req.body.cnpj;
  const sql = `DELETE FROM cadastro_ong WHERE cnpj=?`;
  db.run(sql, [cnpj], function (err) {
    if (err) {
      res.status(500).send("Erro interno do servidor: " + err);
    } else {
      res.send("ONG excluída!");
    }
  });
});

var server = app.listen(port, function () {
  console.log("Servidor rodando na porta " + port);
});

server.timeout = 120000; // Define timeout de 120 segundos
