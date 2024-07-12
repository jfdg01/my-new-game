package com.kandclay.utils;

import com.badlogic.gdx.math.Interpolation;
import de.eskalon.commons.screen.transition.impl.GLTransitionsShaderTransition;

public class Shaders {

    public static class Transitions {
        public static final String BOWTIE = "// Author: huynx\n" +
            "// License: MIT\n" +
            "\n" +
            "vec2 bottom_left = vec2(0.0, 1.0);\n" +
            "vec2 bottom_right = vec2(1.0, 1.0);\n" +
            "vec2 top_left = vec2(0.0, 0.0);\n" +
            "vec2 top_right = vec2(1.0, 0.0);\n" +
            "vec2 center = vec2(0.5, 0.5);\n" +
            "\n" +
            "float check(vec2 p1, vec2 p2, vec2 p3)\n" +
            "{\n" +
            "  return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);\n" +
            "}\n" +
            "\n" +
            "bool PointInTriangle (vec2 pt, vec2 p1, vec2 p2, vec2 p3)\n" +
            "{\n" +
            "    bool b1, b2, b3;\n" +
            "    b1 = check(pt, p1, p2) < 0.0;\n" +
            "    b2 = check(pt, p2, p3) < 0.0;\n" +
            "    b3 = check(pt, p3, p1) < 0.0;\n" +
            "    return ((b1 == b2) && (b2 == b3));\n" +
            "}\n" +
            "\n" +
            "bool in_left_triangle(vec2 p){\n" +
            "  vec2 vertex1, vertex2, vertex3;\n" +
            "  vertex1 = vec2(progress, 0.5);\n" +
            "  vertex2 = vec2(0.0, 0.5-progress);\n" +
            "  vertex3 = vec2(0.0, 0.5+progress);\n" +
            "  if (PointInTriangle(p, vertex1, vertex2, vertex3))\n" +
            "  {\n" +
            "    return true;\n" +
            "  }\n" +
            "  return false;\n" +
            "}\n" +
            "\n" +
            "bool in_right_triangle(vec2 p){\n" +
            "  vec2 vertex1, vertex2, vertex3;\n" +
            "  vertex1 = vec2(1.0-progress, 0.5);\n" +
            "  vertex2 = vec2(1.0, 0.5-progress);\n" +
            "  vertex3 = vec2(1.0, 0.5+progress);\n" +
            "  if (PointInTriangle(p, vertex1, vertex2, vertex3))\n" +
            "  {\n" +
            "    return true;\n" +
            "  }\n" +
            "  return false;\n" +
            "}\n" +
            "\n" +
            "float blur_edge(vec2 bot1, vec2 bot2, vec2 top, vec2 testPt)\n" +
            "{\n" +
            "  vec2 lineDir = bot1 - top;\n" +
            "  vec2 perpDir = vec2(lineDir.y, -lineDir.x);\n" +
            "  vec2 dirToPt1 = bot1 - testPt;\n" +
            "  float dist1 = abs(dot(normalize(perpDir), dirToPt1));\n" +
            "  \n" +
            "  lineDir = bot2 - top;\n" +
            "  perpDir = vec2(lineDir.y, -lineDir.x);\n" +
            "  dirToPt1 = bot2 - testPt;\n" +
            "  float min_dist = min(abs(dot(normalize(perpDir), dirToPt1)), dist1);\n" +
            "  \n" +
            "  if (min_dist < 0.005) {\n" +
            "    return min_dist / 0.005;\n" +
            "  }\n" +
            "  else  {\n" +
            "    return 1.0;\n" +
            "  };\n" +
            "}\n" +
            "\n" +
            "\n" +
            "vec4 transition (vec2 uv) {\n" +
            "  if (in_left_triangle(uv))\n" +
            "  {\n" +
            "    if (progress < 0.1)\n" +
            "    {\n" +
            "      return getFromColor(uv);\n" +
            "    }\n" +
            "    if (uv.x < 0.5)\n" +
            "    {\n" +
            "      vec2 vertex1 = vec2(progress, 0.5);\n" +
            "      vec2 vertex2 = vec2(0.0, 0.5-progress);\n" +
            "      vec2 vertex3 = vec2(0.0, 0.5+progress);\n" +
            "      return mix(\n" +
            "        getFromColor(uv),\n" +
            "        getToColor(uv),\n" +
            "        blur_edge(vertex2, vertex3, vertex1, uv)\n" +
            "      );\n" +
            "    }\n" +
            "    else\n" +
            "    {\n" +
            "      if (progress > 0.0)\n" +
            "      {\n" +
            "        return getToColor(uv);\n" +
            "      }\n" +
            "      else\n" +
            "      {\n" +
            "        return getFromColor(uv);\n" +
            "      }\n" +
            "    }    \n" +
            "  }\n" +
            "  else if (in_right_triangle(uv))\n" +
            "  {\n" +
            "    if (uv.x >= 0.5)\n" +
            "    {\n" +
            "      vec2 vertex1 = vec2(1.0-progress, 0.5);\n" +
            "      vec2 vertex2 = vec2(1.0, 0.5-progress);\n" +
            "      vec2 vertex3 = vec2(1.0, 0.5+progress);\n" +
            "      return mix(\n" +
            "        getFromColor(uv),\n" +
            "        getToColor(uv),\n" +
            "        blur_edge(vertex2, vertex3, vertex1, uv)\n" +
            "      );  \n" +
            "    }\n" +
            "    else\n" +
            "    {\n" +
            "      return getFromColor(uv);\n" +
            "    }\n" +
            "  }\n" +
            "  else {\n" +
            "    return getFromColor(uv);\n" +
            "  }\n" +
            "}";

        public static final String HEART = "// Author: gre\n" +
            "// License: MIT\n" +
            "\n" +
            "float inHeart (vec2 p, vec2 center, float size) {\n" +
            "  if (size==0.0) return 0.0;\n" +
            "  vec2 o = (p-center)/(1.6*size);\n" +
            "  float a = o.x*o.x+o.y*o.y-0.3;\n" +
            "  return step(a*a*a, o.x*o.x*o.y*o.y*o.y);\n" +
            "}\n" +
            "vec4 transition (vec2 uv) {\n" +
            "  return mix(\n" +
            "    getFromColor(uv),\n" +
            "    getToColor(uv),\n" +
            "    inHeart(uv, vec2(0.5, 0.4), progress)\n" +
            "  );\n" +
            "}\n";

        public static final String WIND = "// Author: gre\n" +
            "// License: MIT\n" +
            "\n" +
            "// Custom parameters\n" +
            "uniform float size; // = 0.2\n" +
            "\n" +
            "float rand (vec2 co) {\n" +
            "  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);\n" +
            "}\n" +
            "\n" +
            "vec4 transition (vec2 uv) {\n" +
            "  float r = rand(vec2(0, uv.y));\n" +
            "  float m = smoothstep(0.0, -size, uv.x*(1.0-size) + size*r - (progress * (1.0 + size)));\n" +
            "  return mix(\n" +
            "    getFromColor(uv),\n" +
            "    getToColor(uv),\n" +
            "    m\n" +
            "  );\n" +
            "}\n";
    }

    public static GLTransitionsShaderTransition createTransition(String string, float duration) {
        float progress = 0;
        GLTransitionsShaderTransition transition = new GLTransitionsShaderTransition(string, duration, Interpolation.smooth);

        transition.getProgram().bind();
        transition.getProgram().setUniformf("progress", progress);
        return transition;
    }
}
