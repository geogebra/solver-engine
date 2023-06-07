import { describe, expect, it } from 'vitest';
import { substituteTree, pathToArray, arrayToPath } from '../src/paths';
import { latexToTree, treeToJson } from '../src/parser';

describe('Paths Unit Tests', () => {
  describe('pathToArray', () => {
    it('should return an empty array for the root path', () => {
      expect(pathToArray('.')).to.deep.equal([]);
    });
    it("should return [1, 12, 3] for './1/12/3'", () => {
      expect(pathToArray('./1/12/3')).to.deep.equal([1, 12, 3]);
    });
    it("should return [2, 1] for '2/1'", () => {
      expect(pathToArray('2/1')).to.deep.equal([2, 1]);
    });
  });

  describe('arrayToPath', () => {
    it("should turn [] into '.'", () => {
      expect(arrayToPath([])).to.equal('.');
    });
    it("should return './1/12/3' for [1, 12, 3]", () => {
      expect(arrayToPath([1, 12, 3])).to.equal('./1/12/3');
    });
    it("should return '2/1' for [2, 1]", () => {
      expect(arrayToPath([2, 1])).to.equal('./2/1');
    });
  });

  describe('substituteTree', () => {
    it('should get "2" when substituting "1+1" with "2" at []', () => {
      const parent = latexToTree('1+1');
      const child = latexToTree('2');
      const path = [];
      const result = substituteTree(parent, child, path);
      expect(result).to.deep.equal({ type: 'Number', value: '2', path: '.' });
      expect(treeToJson(result)).to.deep.equal(['2']);
    });
    it('should substitute "1+5" at [1] in "x*(1+2+3)" to get "x*(1+5)"', () => {
      const parent = latexToTree('x*(1+2+3)');
      const child = latexToTree('1+5');
      const path = [1];
      const result = substituteTree(parent, child, path);
      expect(result).to.deep.equal({
        type: 'Product',
        path: '.',
        args: [
          { type: 'Variable', value: 'x', path: './0' },
          {
            type: 'Sum',
            decorators: ['RoundBracket'],
            path: './1',
            args: [
              { type: 'Number', value: '1', path: './1/0' },
              { type: 'Number', value: '5', path: './1/1' },
            ],
          },
        ],
      });
      expect(treeToJson(result)).to.deep.equal([
        'Product',
        ['x'],
        [['Sum', 'RoundBracket'], ['1'], ['5']],
      ]);
    });
    it('should throw an exception for an invalid path', () => {
      expect(() => {
        substituteTree(latexToTree('1+1'), latexToTree('2'), [0, 0]);
      }).to.throw('Invalid path');
    });
  });
});
