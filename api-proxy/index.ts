import express, { Request, Response, Application } from 'express';
import { AsciiMath } from 'asciimath-parser';
import { api, latexToSolver } from '@geogebra/solver-sdk';
import cors from 'cors';

const app: Application = express();
app.use(cors());
app.use(express.json({ type: 'application/json' }));
const port = 3000;
const asciiMath = new AsciiMath({ display: false });

app.get('/', (req: Request, res: Response) => {
  res.send(
    'Use POST requests to /ascii2latex send json body with "ascii" field, or /selectPlans (send json body with "ascii" or "latex" field).'
  );
});

app.get('/version', async (req: Request, res: Response) => {
  const solverVersion = await api.versionInfo();
  res.json({ ...solverVersion, proxyVersion: '0.0.1' });
});

app.post('/ascii2latex', (req: Request, res: Response) => {
  if (!req.body.ascii) {
    res.status(400).send('Need to send "ascii" field in json body.');
    return;
  }
  try {
    res.json({ latex: asciiMath.toTex(req.body.ascii) });
  } catch (error) {
    if (error instanceof Error) res.status(400).send('Error: ' + error.message);
    else res.status(400).send('Error: ' + error);
  }
});

app.post('/selectPlans', async (req: Request, res: Response) => {
  if (!req.body.ascii && !req.body.latex) {
    res
      .status(400)
      .send('Need to specify "ascii" or "latex" input in json body.');
    return;
  }
  try {
    const latex = req.body.latex || asciiMath.toTex(req.body.ascii);
    const input = latexToSolver(latex);
    const result = await api.selectPlans(input, 'latex', req.body.context);
    res.json(result);
  } catch (error) {
    if (error instanceof Error) res.status(400).send('Error: ' + error.message);
    else res.status(400).send('Error: ' + error);
  }
});

app.listen(port, () => {
  console.log(`Server is listening at http://localhost:${port}`);
});
